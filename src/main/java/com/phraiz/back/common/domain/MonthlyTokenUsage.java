package com.phraiz.back.common.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(
        name = "monthly_token_usage",
        uniqueConstraints = @UniqueConstraint(name = "uq_member_month", columnNames = {"member_id", "month"})
)
public class MonthlyTokenUsage {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false, length = 255)
    private String memberId;

    // "2025-09" 같은 월 키(UTC 기준 권장)
    @Column(name = "month", nullable = false, length = 7)
    private String month;

    @Column(name = "used_tokens", nullable = false)
    private long usedTokens;

    @Version
    private Long version; // 낙관적 잠금

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected MonthlyTokenUsage() {}

    public MonthlyTokenUsage(String memberId, String month) {
        this.memberId = memberId;
        this.month = month;
        this.usedTokens = 0L;
        this.updatedAt = LocalDateTime.now();
    }

    // getter/setter 생략
}
