package com.phraiz.back.common.repository;

import com.phraiz.back.common.domain.MonthlyTokenUsage;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MonthlyTokenUsageRepository extends JpaRepository<MonthlyTokenUsage, Long> {

    Optional<MonthlyTokenUsage> findByMemberIdAndMonth(String memberId, String month);

    // 원자적 증가(영향받은 row 갯수 반환)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update MonthlyTokenUsage m
           set m.usedTokens = m.usedTokens + :inc,
               m.updatedAt = CURRENT_TIMESTAMP
         where m.memberId = :memberId
           and m.month = :month
    """)
    int incrementUsedTokens(
            @Param("memberId") String memberId,
            @Param("month") String month,
            @Param("inc") long increment
    );
}

