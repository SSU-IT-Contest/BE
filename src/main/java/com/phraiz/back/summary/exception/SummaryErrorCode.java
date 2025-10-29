package com.phraiz.back.summary.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.phraiz.back.common.exception.ErrorCode;
import lombok.Getter;

@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum SummaryErrorCode implements ErrorCode {

    INVALID_INPUT(400, "CLT001", "사용자 입력 값이 올바르지 않습니다.", "SUMMARY"),

    //
    MONTHLY_TOKEN_LIMIT_EXCEEDED(400, "CLT002", "월 토큰 한도를 초과하였습니다.", "SUMMARY"),

    PLAN_NOT_ACCESSED(400, "CLT003", "무료 요금제 사용자는 이용하실 수 없습니다.", "SUMMARY"),
    PLAN_LIMIN_EXCEEDED(400, "CLT004", "무료 요금제 제한이 초과되었습니다. 요금제를 업데이트하세요.", "SUMMARY"),
    
    HISTORY_NOT_FOUND(404, "CLT005", "히스토리를 찾을 수 없습니다.", "SUMMARY"),
    CONTENT_NOT_FOUND(404, "CLT006", "요청한 내용을 찾을 수 없습니다.", "SUMMARY"),

    // 보안 위협
    CSRF_ATTACK_DETECTED(403, "SEC009", "위조된 요청이 감지되었습니다.", "SECURITY"),
    UNAUTHORIZED_CLIENT(401, "SEC010", "인증되지 않은 클라이언트입니다.", "SECURITY"),

    // 파일 관련
    FILE_INVALID_FORMAT(400, "FILE401", "지원하지 않는 파일 형식입니다.", "FILE_PROCESS"),
    FILE_IS_EMPTY(400, "FILE402", "업로드된 파일이 비어 있습니다.", "FILE_PROCESS"),
    FILE_ENCRYPTED(400, "FILE403", "비밀번호로 보호된 파일은 처리할 수 없습니다.", "FILE_PROCESS"),
    FILE_TEXT_EMPTY(400, "FILE404", "파일에서 유효한 텍스트를 추출하지 못했습니다.", "FILE_PROCESS"),
    FILE_TOO_LARGE(400, "FILE405", "파일 크기가 50MB를 초과했습니다.", "FILE_PROCESS"),
    FILE_READ_FAILED(500, "FILE501", "파일을 읽는 도중 서버 오류가 발생했습니다.", "FILE_PROCESS"),


    // 요약 모드 관련
    INVALID_MODE(400, "SUMMARY401", "잘못된 요약 모드입니다.", "SUMMARY_PROCESS");

    private final int status;
    private final String code;
    private final String message;
    private final String service;

    SummaryErrorCode(int status, String code, String message, String service) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.service = service;
    }
}
