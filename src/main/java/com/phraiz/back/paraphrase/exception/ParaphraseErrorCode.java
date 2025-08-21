package com.phraiz.back.paraphrase.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.phraiz.back.common.exception.ErrorCode;
import lombok.Getter;

@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ParaphraseErrorCode implements ErrorCode {

    INVALID_INPUT(400, "CLT001", "사용자 입력 값이 올바르지 않습니다.", "PARAPHRASE"),

    //
    MONTHLY_TOKEN_LIMIT_EXCEEDED(400, "CLT002", "월 토큰 한도를 초과하였습니다.", "PARAPHRASE"),

    PLAN_NOT_ACCESSED(400, "CLT003", "무료 요금제 사용자는 이용하실 수 없습니다.", "PARAPHRASE"),
    PLAN_LIMIN_EXCEEDED(400, "CLT004", "무료 요금제 제한이 초과되었습니다. 요금제를 업데이트하세요.", "PARAPHRASE"),
    // 보안 위협
    CSRF_ATTACK_DETECTED(403, "SEC009", "위조된 요청이 감지되었습니다.", "SECURITY"),
    UNAUTHORIZED_CLIENT(401, "SEC010", "인증되지 않은 클라이언트입니다.", "SECURITY");

    private final int status;
    private final String code;
    private final String message;
    private final String service;

    ParaphraseErrorCode(int status, String code, String message, String service) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.service = service;
    }
}