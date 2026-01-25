package com.phraiz.back.payment.dto.request;

// 구독 해지 및 환불 요청 DTO를 Record로 변환
public record SubscriptionActionRequestDTO(
        String tossPaymentKey // 특정 결제 건을 식별하기 위한 paymentKey
) {
}