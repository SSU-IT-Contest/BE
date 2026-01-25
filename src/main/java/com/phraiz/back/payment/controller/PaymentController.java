package com.phraiz.back.payment.controller;

import com.phraiz.back.common.util.SecurityUtil;
import com.phraiz.back.payment.dto.request.PaymentRequestDTO;
import com.phraiz.back.payment.dto.request.SubscriptionActionRequestDTO;
import com.phraiz.back.payment.dto.response.PaymentResponseDTO;
import com.phraiz.back.payment.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payment")
public class PaymentController {

    /*
        1. 결제
            1-1. 월 요금 결제(구독제)
            1-2. 연 요금 결제(구독제)
            1-3. 추가 토큰 결제
        2. 환불(문의)
            2-1. 미사용 환불
                - 월요금제/연요금제 가능.
                - 결제 후 일주일 내로 환불 요청
            2-2. 사용 후 환불
                - 연요금제만 가능.
                - 차액만 환불(월 단위)
        3. 구독 해지
        4. 할인
        5. 3개월 무료 체험
        6. 요금제 변경 - 해당 요금제 사용 가능 기간 초기화?
     */

    private final PaymentService paymentService; // 서비스 계층 의존성 주입

    /**
     * 결제의 금액을 세션에 임시저장
     * 결제 과정에서 악의적으로 결제 금액이 바뀌는 것을 확인하는 용도
     */
    @PostMapping("/saveAmount")
    public ResponseEntity<Void> tempsave(HttpSession session, @RequestBody PaymentRequestDTO saveAmountRequest) {
        session.setAttribute(saveAmountRequest.orderId(), saveAmountRequest.amount());
        return ResponseEntity.ok().build();
    }

    // 1-1 & 1-2. 월/연 요금제 결제 (구독)
    @PostMapping("/subscription")
    public ResponseEntity<PaymentResponseDTO> subscribe(@RequestBody PaymentRequestDTO dto, HttpServletRequest request) {
        String memberId = SecurityUtil.getCurrentMemberId();
        PaymentResponseDTO result = paymentService.processSubscriptionPayment(memberId, dto);
        return ResponseEntity.ok(result);
    }

    // 3. 구독 해지(환불 X, 다음 달부터 요금 안 나감)
    @PostMapping("/subscription/cancel")
    public ResponseEntity<PaymentResponseDTO> cancelSubscription(@RequestBody SubscriptionActionRequestDTO dto, HttpServletRequest request) {
        String memberId = SecurityUtil.getCurrentMemberId();
        PaymentResponseDTO result = paymentService.cancelSubscription(memberId, dto);
        return ResponseEntity.ok(result);
    }

    // 2-1. 미사용 환불 (전액 환불)
    @PostMapping("/refund/unused")
    public ResponseEntity<PaymentResponseDTO> refundUnused(@RequestBody SubscriptionActionRequestDTO dto, HttpServletRequest request) {
        String memberId = SecurityUtil.getCurrentMemberId();
        PaymentResponseDTO result = paymentService.refundUnused(memberId, dto);
        return ResponseEntity.ok(result);
    }


}
