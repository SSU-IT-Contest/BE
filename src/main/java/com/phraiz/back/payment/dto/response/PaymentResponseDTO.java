package com.phraiz.back.payment.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponseDTO {
    private String status;        // 성공 또는 실패 상태
    private String message;       // 응답 메시지
    private String paymentKey;    // 처리된 결제 건의 paymentKey
}