package com.phraiz.back.paraphrase.service;

import com.phraiz.back.common.exception.custom.BusinessLogicException;
import com.phraiz.back.common.service.OpenAIService;
import com.phraiz.back.common.service.RedisService;
import com.phraiz.back.common.enums.Plan;
import com.phraiz.back.common.util.GptTokenUtil;
import com.phraiz.back.member.domain.Member;
import com.phraiz.back.member.exception.MemberErrorCode;
import com.phraiz.back.member.repository.MemberRepository;
import com.phraiz.back.paraphrase.dto.request.ParaphraseRequestDTO;
import com.phraiz.back.paraphrase.dto.response.ParaphraseResponseDTO;
import com.phraiz.back.paraphrase.enums.ParaphrasePrompt;
import com.phraiz.back.paraphrase.exception.ParaphraseErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final MemberRepository memberRepository;

    public ParaphraseResponseDTO paraphraseStandard(String memberId, ParaphraseRequestDTO paraphraseRequestDTO){
        return paraphrase(memberId, paraphraseRequestDTO.getText(), ParaphrasePrompt.STANDARD.getPrompt());
    }
    public ParaphraseResponseDTO paraphraseAcademic(String memberId, ParaphraseRequestDTO paraphraseRequestDTO){
        return paraphrase(memberId, paraphraseRequestDTO.getText(), ParaphrasePrompt.ACADEMIC.getPrompt());
    }
    public ParaphraseResponseDTO paraphraseCreative(String memberId, ParaphraseRequestDTO paraphraseRequestDTO){
        return paraphrase(memberId, paraphraseRequestDTO.getText(), ParaphrasePrompt.CREATIVE.getPrompt());
    }
    public ParaphraseResponseDTO paraphraseFluency(String memberId, ParaphraseRequestDTO paraphraseRequestDTO){
        return paraphrase(memberId, paraphraseRequestDTO.getText(), ParaphrasePrompt.FLUENCY.getPrompt());
    }
    public ParaphraseResponseDTO paraphraseExperimental(String memberId, ParaphraseRequestDTO paraphraseRequestDTO){
        return paraphrase(memberId, paraphraseRequestDTO.getText(), ParaphrasePrompt.EXPERIMENTAL.getPrompt());
    }
    public ParaphraseResponseDTO paraphraseCustom(String memberId, ParaphraseRequestDTO paraphraseRequestDTO){
        // target 값 추출
        String paraphraseMode = paraphraseRequestDTO.getUserRequestMode();
        if(paraphraseMode == null){
            throw new BusinessLogicException(ParaphraseErrorCode.INVALID_INPUT);
        }
        return paraphrase(memberId, paraphraseRequestDTO.getText(), paraphraseMode);
    }


        // 1. paraphrase 메서드
    private ParaphraseResponseDTO paraphrase(String memberId, String paraphraseRequestedText, String paraphraseMode){

        // 1. 로그인한 멤버 정보 가져오기 - 멤버의 요금제 정보
        Member member=memberRepository.findById(memberId).orElseThrow(()->new BusinessLogicException(MemberErrorCode.USER_NOT_FOUND));

        // 2. 요금제 정책에 따라 다음 로직 분기
        // 2-1. 남은 월 토큰 확인 (DB나 Redis에서 누적 사용량 조회)
        Plan userPlan = Plan.fromId(member.getPlanId());
        validateRemainingMonthlyTokens(memberId, userPlan, paraphraseRequestedText);

        // 3. paraphrase 처리 (service 호출)
        String result = openAIService.callParaphraseOpenAI(paraphraseRequestedText, paraphraseMode);

        /**
         * Todo. 히스토리 업데이트
         */
        // 4. 내용 저장 (히스토리 업데이트)

        // 5. 사용량 업데이트
        //    - 월 토큰 사용량 증가
        redisService.incrementMonthlyUsage(memberId, YearMonth.now().toString(), GptTokenUtil.estimateTokenCount(paraphraseRequestedText));

        // 6. result return
        ParaphraseResponseDTO responseDTO = ParaphraseResponseDTO.builder().result(result).build();
        return responseDTO;
    }

    private void validateRemainingMonthlyTokens(String memberId, Plan plan, String text){

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
    }

    private long findOrInitializeMonthlyUsage(String memberId, String month){
        // 1. Redis에서 조회
        Long value = redisService.getMonthlyUsage(memberId, month);
        if (value >= 0) {
            // Redis에 값이 있으면 바로 반환
            return value;
        }

        /**
         * Todo. Redis에 없으면 DB에서 조회
         */
        // 2-1. Redis에 없으면 DB에서 조회
//        Long dbValue = userTokenUsageRepository
//                .findUsedTokens(memberId, month)  // ex) Optional<Long>
//                .orElse(0L);
        Long dbValue = 0L;

        // 2-2. Redis에 캐싱
        redisService.setMonthlyUsage(memberId, month, dbValue);

        return dbValue;
    }

}
