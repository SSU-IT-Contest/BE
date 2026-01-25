package com.phraiz.back.payment.dto.request;

// 결제 요청 DTO를 Record로 변환
public record PaymentRequestDTO(
        String tossPaymentKey, // 토스 결제 승인에 필요한 paymentKey
        String orderId,        // 주문 ID
        Long amount,           // 결제 금액
        String plan,           // 플랜 타입
        String payType         // 월 요금/연 요금
) {
}