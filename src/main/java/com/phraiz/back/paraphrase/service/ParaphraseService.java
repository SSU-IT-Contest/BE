package com.phraiz.back.paraphrase.service;

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
import com.phraiz.back.paraphrase.domain.ParaphraseContent;
import com.phraiz.back.paraphrase.domain.ParaphraseHistory;
import com.phraiz.back.paraphrase.dto.request.ParaphraseRequestDTO;
import com.phraiz.back.paraphrase.dto.response.ParaphraseResponseDTO;
import com.phraiz.back.paraphrase.enums.ParaphrasePrompt;
import com.phraiz.back.paraphrase.exception.ParaphraseErrorCode;
import com.phraiz.back.paraphrase.repository.ParaphraseContentRepository;
import com.phraiz.back.paraphrase.repository.ParaphraseHistoryRepository;
import com.phraiz.back.summary.exception.SummaryErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ParaphraseService {

    private final OpenAIService openAIService;
    private final RedisService redisService;
    private final ParaphraseHistoryService paraphraseHistoryService;
    private final ParaphraseContentRepository paraphraseContentRepository;
    private final ParaphraseHistoryRepository paraphraseHistoryRepository;
    private final MonthlyTokenUsageService tokenUsageService;
    private final MemberRepository memberRepository;

    public ParaphraseResponseDTO paraphraseStandard(String memberId, ParaphraseRequestDTO paraphraseRequestDTO){
        return paraphrase(memberId, paraphraseRequestDTO.getText(), ParaphrasePrompt.STANDARD.getPrompt(), paraphraseRequestDTO.getScale(),
                paraphraseRequestDTO.getFolderId(), paraphraseRequestDTO.getHistoryId(), "standard");
    }
    public ParaphraseResponseDTO paraphraseAcademic(String memberId, ParaphraseRequestDTO paraphraseRequestDTO){
        return paraphrase(memberId, paraphraseRequestDTO.getText(), ParaphrasePrompt.ACADEMIC.getPrompt(), paraphraseRequestDTO.getScale(),
                paraphraseRequestDTO.getFolderId(), paraphraseRequestDTO.getHistoryId(), "academic");
    }
    public ParaphraseResponseDTO paraphraseCreative(String memberId, ParaphraseRequestDTO paraphraseRequestDTO){
        return paraphrase(memberId, paraphraseRequestDTO.getText(), ParaphrasePrompt.CREATIVE.getPrompt(), paraphraseRequestDTO.getScale(),
                paraphraseRequestDTO.getFolderId(), paraphraseRequestDTO.getHistoryId(), "creative");
    }
    public ParaphraseResponseDTO paraphraseFluency(String memberId, ParaphraseRequestDTO paraphraseRequestDTO){
        return paraphrase(memberId, paraphraseRequestDTO.getText(), ParaphrasePrompt.FLUENCY.getPrompt(), paraphraseRequestDTO.getScale(),
                paraphraseRequestDTO.getFolderId(), paraphraseRequestDTO.getHistoryId(), "fluency");
    }
    public ParaphraseResponseDTO paraphraseExperimental(String memberId, ParaphraseRequestDTO paraphraseRequestDTO){
        return paraphrase(memberId, paraphraseRequestDTO.getText(), ParaphrasePrompt.EXPERIMENTAL.getPrompt(), paraphraseRequestDTO.getScale(),
                paraphraseRequestDTO.getFolderId(), paraphraseRequestDTO.getHistoryId(), "experimental");
    }
    public ParaphraseResponseDTO paraphraseCustom(String memberId, ParaphraseRequestDTO paraphraseRequestDTO){
        // free 요금제 사용자는 사용 불가능
        Member member=memberRepository.findById(memberId).orElseThrow(()->new BusinessLogicException(MemberErrorCode.USER_NOT_FOUND));
        Plan userPlan = Plan.fromId(member.getPlanId());
        if(userPlan == Plan.FREE){
            throw new BusinessLogicException(SummaryErrorCode.PLAN_NOT_ACCESSED);
        }

        // target 값 추출
        String paraphraseMode = paraphraseRequestDTO.getUserRequestMode();
        if(paraphraseMode == null){
            throw new BusinessLogicException(ParaphraseErrorCode.INVALID_INPUT);
        }
        return paraphrase(memberId, paraphraseRequestDTO.getText(), paraphraseMode, paraphraseRequestDTO.getScale(),
                paraphraseRequestDTO.getFolderId(), paraphraseRequestDTO.getHistoryId(), "custom");
    }

    // 1. paraphrase 메서드
    private ParaphraseResponseDTO paraphrase(String memberId,
                                             String paraphraseRequestedText,
                                             String paraphraseMode, int scale,
                                             Long folderId,
                                             Long historyId,
                                             String mode
                                             ){
        long remainingToken = 0;

        // 1. 로그인한 멤버 정보 가져오기 - 멤버의 요금제 정보
        Member member=memberRepository.findById(memberId).orElseThrow(()->new BusinessLogicException(MemberErrorCode.USER_NOT_FOUND));

        // 2. 요금제 정책에 따라 다음 로직 분기
        // 2-1. 남은 월 토큰 확인 (DB나 Redis에서 누적 사용량 조회)
        Plan userPlan = Plan.fromId(member.getPlanId());
        if(userPlan != Plan.PRO){
            // 남은 토큰 < 요청 토큰의 경우 예외 발생
            remainingToken = validateRemainingMonthlyTokens(memberId, userPlan, paraphraseRequestedText);
        }

        // 3. paraphrase 처리 (service 호출)
        String result = openAIService.callParaphraseOpenAI(paraphraseRequestedText, paraphraseMode, scale);

        // 4. 내용 저장 (Content로 저장)
        HistoryMetaDTO metaDTO = saveParaphraseContent(  // ★ 변경됨
                memberId,
                folderId,
                historyId,
                paraphraseRequestedText,  // 원본 텍스트
                result,                     // 패러프레이징 결과
                scale,
                mode,
                paraphraseMode
        );

        // 5. 사용량 업데이트
        //    - 월 토큰 사용량 증가
        incrementMonthlyUsage(memberId, YearMonth.now().toString(), GptTokenUtil.estimateTokenCount(paraphraseRequestedText));

        // 6. result return
        ParaphraseResponseDTO responseDTO = ParaphraseResponseDTO.builder()
                .resultHistoryId(metaDTO.id())
                .name(metaDTO.name())
                .originalText(paraphraseRequestedText)
                .paraphrasedText(result)
                .sequenceNumber(metaDTO.sequenceNumber())
                .remainingToken(remainingToken)
                .build();
        return responseDTO;
    }
    
    // 4. Content 저장 로직
    private HistoryMetaDTO saveParaphraseContent(String memberId, Long folderId, Long historyId, 
                                                  String originalText, String paraphrasedText, int scale, String mode, String paraphraseMode) {
        ParaphraseHistory history;
        Integer nextSequenceNumber;
        
        if (historyId != null) {
            // 기존 히스토리에 content 추가
            history = paraphraseHistoryRepository.findById(historyId)
                    .orElseThrow(() -> new BusinessLogicException(ParaphraseErrorCode.HISTORY_NOT_FOUND));
            
            // 현재 content 개수 확인하여 다음 sequence number 계산
            Long contentCount = paraphraseContentRepository.countByHistoryId(historyId);
            nextSequenceNumber = contentCount.intValue() + 1;
            
            // 10개 초과 시 가장 오래된 content 삭제
            if (contentCount >= 10) {
                paraphraseContentRepository.findByHistoryIdOrderBySequenceNumberDesc(historyId)
                        .stream()
                        .skip(9)  // 최신 9개는 유지
                        .forEach(paraphraseContentRepository::delete);
            }
        } else {
            // 새 히스토리 생성
            history = paraphraseHistoryService.createNewHistory(memberId, folderId);
            nextSequenceNumber = 1;
        }

        ParaphraseContent content;
        
        // Content 생성 및 저장
        if(mode.equals("custom")){
            content = ParaphraseContent.builder()
                    .history(history)
                    .originalText(originalText)
                    .paraphrasedText(paraphrasedText)
                    .sequenceNumber(nextSequenceNumber)
                    .scale(scale)
                    .mode(mode)
                    .userRequestMode(paraphraseMode)    // 사용자 지정모드는 따로 모드 세부 내용 저장
                    .build();
        } else {
            content = ParaphraseContent.builder()
                    .history(history)
                    .originalText(originalText)
                    .paraphrasedText(paraphrasedText)
                    .sequenceNumber(nextSequenceNumber)
                    .scale(scale)
                    .mode(mode)
                    .build();
        }        
        
        paraphraseContentRepository.save(content);
        
        // HistoryMetaDTO 반환
        return new HistoryMetaDTO(history.getId(), history.getName(), nextSequenceNumber);
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
            throw new BusinessLogicException(ParaphraseErrorCode.MONTHLY_TOKEN_LIMIT_EXCEEDED, String.format("월 토큰 한도를 초과하였습니다. (요청: %d, 남음: %d)", requestedTokens, remaining));
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

}
