package com.phraiz.back.payment.domain;

import com.phraiz.back.common.enums.Plan;
import com.phraiz.back.member.domain.Member;
import lombok.*;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@Builder
@Table(name = "MemberSubscriptionInfo")
public class MemberSubscriptionInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscription_id")
    private Long subscriptionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member; // Member Entity와 매핑

    @Column(name = "plan_id", nullable = false)
    private Long plan; // Plan Entity와 매핑

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", unique = true) // 1:1 관계이므로 unique
    private TossPayment tossPayment; // TossPayment Entity와 매핑

    @Enumerated(EnumType.STRING)
    @Column(name = "pay_type", nullable = false)
    private PayType payType;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "next_renewal_date")
    private LocalDateTime nextRenewalDate;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true; // 기본값 TRUE

    @Column(name = "initial_amount", nullable = false)
    private Long initialAmount; // 할인 적용 전 금액

    @Column(name = "paid_amount", nullable = false)
    private Long paidAmount; // 실제 결제된 금액

    @Column(name = "discount_rate", precision = 5, scale = 2)
    private BigDecimal discountRate = BigDecimal.ZERO; // 기본값 0.00

    @Column(name = "cancellation_date")
    private LocalDateTime cancellationDate;

    @Column(name = "refund_amount")
    private Long refundAmount;

    @Column(name = "memo", length = 500)
    private String memo;

    // PayType enum 정의
    public enum PayType {
        MONTHLY, ANNUAL;

        public static PayType fromName(String name) {
            return Arrays.stream(values())
                    .filter(p -> p.name().equalsIgnoreCase(name))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 요금제: " + name));
        }
    }

    // 생성자, 빌더 패턴 등 필요에 따라 추가
}