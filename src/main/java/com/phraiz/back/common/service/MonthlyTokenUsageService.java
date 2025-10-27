package com.phraiz.back.common.service;

import com.phraiz.back.common.domain.MonthlyTokenUsage;
import com.phraiz.back.common.repository.MonthlyTokenUsageRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MonthlyTokenUsageService {

    private final MonthlyTokenUsageRepository repo;

    public MonthlyTokenUsageService(MonthlyTokenUsageRepository repo) {
        this.repo = repo;
    }

    /**
     * DB에서 월별 사용량을 조회. 없으면 0행을 생성 후 0 반환.
     * (캐시는 호출자 모듈에서 별도로 처리)
     */
    @Transactional
    public long findOrInitializeUsedTokens(String memberId, String month) {
        return repo.findByMemberIdAndMonth(memberId, month)
                .orElseGet(() -> createRowIfAbsent(memberId, month))
                .getUsedTokens();
    }

    /**
     * 월별 사용량 증가(원자적). 행이 없으면 생성 후 1회 재시도.
     * 최종적으로 증가 후의 최신 usedTokens 값을 반환.
     */
    @Transactional
    public long incrementUsedTokens(String memberId, String month, long increment) {
        int updated = repo.incrementUsedTokens(memberId, month, increment);
        if (updated == 0) {
            // 행이 없었던 경쟁 상황 -> 생성 후 다시 증가
            createRowIfAbsent(memberId, month);
            repo.incrementUsedTokens(memberId, month, increment);
        }
        // 증가 후 최신값 조회
        return repo.findByMemberIdAndMonth(memberId, month)
                .map(MonthlyTokenUsage::getUsedTokens)
                .orElse(0L); // 거의 도달하지 않음
    }

    /**
     * 존재하지 않으면 생성. UNIQUE(member, month) 충돌 시 재조회로 수습.
     */
    @Transactional
    protected MonthlyTokenUsage createRowIfAbsent(String memberId, String month) {
        try {
            return repo.save(new MonthlyTokenUsage(memberId, month));
        } catch (DataIntegrityViolationException e) {
            // 동시성으로 이미 생성된 경우
            return repo.findByMemberIdAndMonth(memberId, month)
                    .orElseThrow(() -> e);
        }
    }
}