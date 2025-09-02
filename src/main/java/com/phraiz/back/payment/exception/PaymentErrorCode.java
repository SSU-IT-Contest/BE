package com.phraiz.back.payment.exception;

import com.phraiz.back.common.exception.ErrorCode;


public enum PaymentErrorCode implements ErrorCode {
    // Payment-specific errors (결제 도메인 관련 에러)
    // 클라이언트 관련 에러 (BusinessException)
    SUBSCRIPTION_NOT_FOUND(404, "CLT001", "해당 결제 키로 구독 정보를 찾을 수 없습니다.", "PAYMENT"),
    UNAUTHORIZED_ACCESS(403, "CLT002", "해당 결제 건에 대한 권한이 없습니다.", "PAYMENT"),
    REFUND_PERIOD_EXPIRED(400, "CLT003", "환불 가능 기간이 만료되었습니다.", "PAYMENT"),
    MEMBER_NOT_FOUND(404, "CLT004", "존재하지 않는 회원입니다.", "PAYMENT"),
    PLAN_NOT_FOUND(404, "CLT005", "존재하지 않는 요금제입니다.", "PAYMENT"),
    INVALID_PAYMENT_STATUS(400, "CLT006", "결제 상태가 유효하지 않습니다.", "PAYMENT"),
    SUBSCRIPTION_ALREADY_CANCELLED(400, "CLT007", "이미 취소된 구독입니다.", "PAYMENT"),
    INSUFFICIENT_FUNDS_FOR_REFUND(400, "CLT008", "환불 가능한 금액이 없습니다.", "PAYMENT"),
    SUBSCRIPTION_ACTIVE(400, "CLT009", "구독 중인 요금제가 있습니다. 구독 해지를 먼저 진행해주세요.", "PAYMENT"),
    AMOUNT_MISMATCH(400, "CLT010", "결제 금액이 일치하지 않습니다.", "PAYMENT"),


    // 서버 관련 에러 (InternalServerException)
    PAYMENT_CONFIRMATION_FAILED(500, "SYS001", "토스페이먼츠 결제 승인에 실패했습니다.", "PAYMENT"),
    TOSS_API_CALL_FAILED(500, "SYS002", "토스페이먼츠 API 통신 중 오류가 발생했습니다.", "PAYMENT")
    ;

    private final int status;
    private final String code;
    private final String message;
    private final String service;

    PaymentErrorCode(int status, String code, String message, String service) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.service = service;
    }

    @Override public int getStatus() { return status; }
    @Override public String getCode() { return code; }
    @Override public String getMessage() { return message; }
    @Override public String getService() { return service; }
}
