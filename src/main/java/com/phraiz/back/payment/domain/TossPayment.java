package com.phraiz.back.payment.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@RequiredArgsConstructor
@Table(name = "TossPayment")
@Builder
public class TossPayment {

    @Id
    // 토스페이먼츠에서 제공하는 paymentKey를 primary key로 사용하므로 @GeneratedValue는 사용하지 않습니다.
    // UUID (VARBINARY) 형태는 String으로 매핑하는 경우 VARCHAR로 컬럼 타입 변경이 필요합니다.
    @Column(name = "payment_id", length = 255, nullable = false)
    private String paymentId;

    // pay_info_id는 더 이상 사용하지 않거나, MemberSubscriptionInfo와 1:1 매핑될 경우 필드 제거 또는 Unique 제거 고려
    @Column(name = "pay_info_id", length = 16, nullable = false, unique = true)
    private Long payInfoId; // 이 필드의 사용 목적을 재고 필요

    @Column(name = "toss_order_id", length = 255, nullable = false)
    private String tossOrderId; // 토스에서 관리하는 별도의 주문 아이디

    @Column(name = "toss_payment_key", length = 255, nullable = false, unique = true)
    private String tossPaymentKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "toss_payment_method", nullable = false)
    private TossPaymentMethod tossPaymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "toss_payment_status", nullable = false)
    private TossPaymentStatus tossPaymentStatus;

    @Column(name = "total_amount", nullable = false)
    private Long totalAmount;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    // TossPaymentMethod enum 정의
    public enum TossPaymentMethod {
        가상계좌, 간편결제, 게임문화상품권, 계좌이체, 도서문화상품권, 문화상품권, 카드, 휴대폰
    }

    // TossPaymentStatus enum 정의
    public enum TossPaymentStatus {
        ABORTED, CANCELED, DONE, EXPIRED, IN_PROGRESS, PARTIAL_CANCELED, READY, WAITING_FOR_DEPOSIT
    }

    // 생성자, 빌더 패턴 등 필요에 따라 추가
}
