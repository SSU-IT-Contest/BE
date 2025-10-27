package com.phraiz.back.summary.service;

import com.phraiz.back.common.dto.response.HistoryMetaDTO;
import com.phraiz.back.common.exception.custom.BusinessLogicException;
import com.phraiz.back.common.service.MonthlyTokenUsageService;
import com.phraiz.back.common.service.OpenAIService;
import com.phraiz.back.common.service.RedisService;
import com.phraiz.back.common.enums.Plan;
import com.phraiz.back.common.util.GptTokenUtil;
import com.phraiz.back.member.domain.Member;
import com.phraiz.back.member.exception.MemberErrorCode;
import com.phraiz.back.member.repository.MemberRepository;
import com.phraiz.back.summary.dto.request.SummaryRequestDTO;
import com.phraiz.back.summary.dto.response.SummaryResponseDTO;
import com.phraiz.back.summary.enums.SummaryPrompt;
import com.phraiz.back.summary.exception.SummaryErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.YearMonth;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SummaryService {

    private final OpenAIService openAIService;
    private final RedisService redisService;
    private final SummaryHistoryService summaryHistoryService;
    private final MonthlyTokenUsageService tokenUsageService;
    private final MemberRepository memberRepository;

    public SummaryResponseDTO oneLineSummary(String memberId, SummaryRequestDTO summaryRequestDTO){
        return summary(memberId, summaryRequestDTO.getText(), SummaryPrompt.ONE_LINE.getPrompt(),
                summaryRequestDTO.getFolderId(), summaryRequestDTO.getHistoryId());
    }

    public SummaryResponseDTO fullSummary(String memberId, SummaryRequestDTO summaryRequestDTO){
        return summary(memberId, summaryRequestDTO.getText(), SummaryPrompt.FULL.getPrompt(),
                summaryRequestDTO.getFolderId(), summaryRequestDTO.getHistoryId());
    }

    public SummaryResponseDTO paragraphSummary(String memberId, SummaryRequestDTO summaryRequestDTO){
        return summary(memberId, summaryRequestDTO.getText(), SummaryPrompt.PARAGRAPH.getPrompt(),
                summaryRequestDTO.getFolderId(), summaryRequestDTO.getHistoryId());
    }

    public SummaryResponseDTO keyPointSummary(String memberId, SummaryRequestDTO summaryRequestDTO){
        return summary(memberId, summaryRequestDTO.getText(), SummaryPrompt.KEY_POINT.getPrompt(),
                summaryRequestDTO.getFolderId(), summaryRequestDTO.getHistoryId());
    }

    public SummaryResponseDTO questionBasedSummary(String memberId, SummaryRequestDTO summaryRequestDTO){
        // free 요금제 사용자는 사용 불가능
        Member member=memberRepository.findById(memberId).orElseThrow(()->new BusinessLogicException(MemberErrorCode.USER_NOT_FOUND));
        Plan userPlan = Plan.fromId(member.getPlanId());
        if(userPlan == Plan.FREE){
            throw new BusinessLogicException(SummaryErrorCode.PLAN_NOT_ACCESSED);
        }

        // question 값 추출
        String question = summaryRequestDTO.getQuestion();
        if(question == null){
            throw new BusinessLogicException(SummaryErrorCode.INVALID_INPUT);
        }
        // 프롬프트에 삽입
        String prompt = String.format(SummaryPrompt.QUESTION_BASED.getPrompt(), question);
        return summary(memberId, summaryRequestDTO.getText(), prompt,
                summaryRequestDTO.getFolderId(), summaryRequestDTO.getHistoryId());
    }

    public SummaryResponseDTO targetedSummary(String memberId, SummaryRequestDTO summaryRequestDTO){
        // free 요금제 사용자는 사용 불가능
        Member member=memberRepository.findById(memberId).orElseThrow(()->new BusinessLogicException(MemberErrorCode.USER_NOT_FOUND));
        Plan userPlan = Plan.fromId(member.getPlanId());
        if(userPlan == Plan.FREE){
            throw new BusinessLogicException(SummaryErrorCode.PLAN_NOT_ACCESSED);
        }
        // target 값 추출
        String target = summaryRequestDTO.getTarget();
        if(target == null){
            throw new BusinessLogicException(SummaryErrorCode.INVALID_INPUT);
        }
        // 프롬프트에 삽입
        String prompt = String.format(SummaryPrompt.TARGETED.getPrompt(), target);
        return summary(memberId, summaryRequestDTO.getText(), prompt,
                summaryRequestDTO.getFolderId(), summaryRequestDTO.getHistoryId());
    }

    // 1. 요약 메서드
    private SummaryResponseDTO summary(String memberId,
                                       String summarizeRequestedText,
                                       String summarizeMode,
                                       Long folderId,
                                       Long historyId){

        long remainingToken = 0;

        // 1. 로그인한 멤버 정보 가져오기 - 멤버의 요금제 정보
        Member member=memberRepository.findById(memberId).orElseThrow(()->new BusinessLogicException(MemberErrorCode.USER_NOT_FOUND));

        // 2. 요금제 정책에 따라 다음 로직 분기
        // 2-1. 남은 월 토큰 확인 (DB나 Redis에서 누적 사용량 조회)
        Plan userPlan = Plan.fromId(member.getPlanId());
        if(userPlan != Plan.PRO){
            remainingToken = validateRemainingMonthlyTokens(memberId, userPlan, summarizeRequestedText);
        }

        // 3. 요약 처리 (service 호출)
        String result = openAIService.callSummaryOpenAI(summarizeRequestedText, summarizeMode);

        // 4. 내용 저장 (히스토리 업데이트)
        HistoryMetaDTO metaDTO = summaryHistoryService.saveOrUpdateHistory(  // ★
                memberId,
                folderId,      // 루트면 null
                historyId,
                result      // content
        );

        // 5. 사용량 업데이트
        //    - 월 토큰 사용량 증가
        incrementMonthlyUsage(memberId, YearMonth.now().toString(), GptTokenUtil.estimateTokenCount(summarizeRequestedText));

        // 6. result return
        SummaryResponseDTO responseDTO = SummaryResponseDTO.builder()
                .historyId(metaDTO.id())
                .name(metaDTO.name())
                .result(result)
                .remainingToken(remainingToken)
                .originalText(summarizeRequestedText)
                .build();
        return responseDTO;
    }

    private long validateRemainingMonthlyTokens(String memberId, Plan plan, String text){

        // 1. 현재까지 사용량
        long usedTokensThisMonth = findOrInitializeMonthlyUsage(memberId, YearMonth.now().toString());

        // 2. 요청 텍스트 토큰 수
        int requestedTokens = GptTokenUtil.estimateTokenCount(text);

        // 3. 남은 토큰
        long remaining = plan.getMaxTokensPerMonth() - usedTokensThisMonth;

        // 4. 남은 월 토큰 < 요청 토큰이면 → 에러 응답 반환
        if (remaining < requestedTokens) {
            throw new BusinessLogicException(SummaryErrorCode.MONTHLY_TOKEN_LIMIT_EXCEEDED, String.format("월 토큰 한도를 초과하였습니다. (요청: %d, 남음: %d)", requestedTokens, remaining));
        }
        return remaining - requestedTokens;
    }

    private long findOrInitializeMonthlyUsage(String memberId, String month){
        // 1. Redis에서 조회
        Long value = redisService.getMonthlyUsage(memberId, month);
        if (value >= 0) {
            // Redis에 값이 있으면 바로 반환
            return value;
        }

        // 2-1. Redis에 없으면 DB에서 조회
        long used = tokenUsageService.findOrInitializeUsedTokens(memberId, month);
        redisService.setMonthlyUsage(memberId, month, used);
        return used;
    }

    private void incrementMonthlyUsage (String memberId, String month, long increment){
        long after = tokenUsageService.incrementUsedTokens(memberId, month, increment);
        // 캐시 동기화(정합성 보장)
        redisService.incrementMonthlyUsage(memberId, month, increment);
    }



    /* ---------- 파일 업로드 ---------- */
    public SummaryResponseDTO uploadFile(String memberId, MultipartFile file, String mode, String target, String question, Long historyId, Long folderId) {
        String text = "";
        // free 요금제 사용자는 사용 불가능
        Member member=memberRepository.findById(memberId).orElseThrow(()->new BusinessLogicException(MemberErrorCode.USER_NOT_FOUND));
        Plan userPlan = Plan.fromId(member.getPlanId());
        if(userPlan == Plan.FREE){
            throw new BusinessLogicException(SummaryErrorCode.PLAN_NOT_ACCESSED);
        }
        // 업로드 파일 존재 여부 검사
        if (file.isEmpty()) {
            throw new BusinessLogicException(SummaryErrorCode.FILE_IS_EMPTY);
        }
        // 파일 확장자 검증
        if (!file.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
            throw new BusinessLogicException(SummaryErrorCode.FILE_INVALID_FORMAT);
        }
        // 파일 용량 검사
        if (file.getSize() > 2 * 1024 * 1024) { // 2MB 초과
            throw new BusinessLogicException(SummaryErrorCode.FILE_TOO_LARGE);
        }


        // 텍스트 추출
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            text = stripper.getText(document);
        } catch (IOException e) {
            e.printStackTrace();
            throw new BusinessLogicException(SummaryErrorCode.FILE_READ_FAILED);
        }

        // 선택된 요약 모드 확인
        SummaryPrompt prompt;
        try {
            prompt = SummaryPrompt.valueOf(mode.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessLogicException(SummaryErrorCode.INVALID_MODE);
        }

        // 프롬프트에 삽입
        if (prompt == SummaryPrompt.TARGETED){
            SummaryRequestDTO requestDTO = SummaryRequestDTO.builder()
                    .historyId(historyId)
                    .folderId(folderId)
                    .target(target)
                    .text(text)
                    .build();
            return targetedSummary(memberId, requestDTO);
        }else if (prompt == SummaryPrompt.QUESTION_BASED){
            SummaryRequestDTO requestDTO = SummaryRequestDTO.builder()
                    .historyId(historyId)
                    .folderId(folderId)
                    .question(question)
                    .text(text)
                    .build();
            return questionBasedSummary(memberId, requestDTO);
        }else {
            return summary(memberId, text, prompt.getPrompt(),
                    folderId, historyId);
        }
    }

}
