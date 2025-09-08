package com.phraiz.back.common.exception.custom;

import com.phraiz.back.common.exception.ErrorCode;

public class InternalServerException extends RuntimeException {

    private final ErrorCode errorCode; // 예외 코드
    private final String detailMessage;

    // 생성자: 예외 코드만 사용하는 경우
    public InternalServerException(ErrorCode errorCode) {
        super(errorCode.getMessage()); // 상위 클래스의 메시지에 예외 코드의 메시지를 전달
        this.errorCode = errorCode;
        this.detailMessage = null; // 세부 메시지가 없는 경우
    }

    // 생성자: 예외 코드와 추가적인 메시지를 함께 사용하는 경우
    public InternalServerException(ErrorCode errorCode, String detailMessage) {
        super(errorCode.getMessage() + ": " + detailMessage); // 상위 클래스의 메시지에 예외 코드의 메시지와 추가적인 메시지를 전달
        this.errorCode = errorCode;
        this.detailMessage = detailMessage;
    }

    // 생성자: 예외 코드 + 원인 예외(cause)
    public InternalServerException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.detailMessage = null;
    }

    // 생성자: 예외 코드 + 추가 메시지 + 원인 예외(cause)
    public InternalServerException(ErrorCode errorCode, String detailMessage, Throwable cause) {
        super(errorCode.getMessage() + ": " + detailMessage, cause);
        this.errorCode = errorCode;
        this.detailMessage = detailMessage;
    }

    // 예외 코드만 반환
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    // HTTP 상태 코드 반환
    public int getStatus() {
        return errorCode.getStatus(); // ExceptionCode에서 상태 코드를 가져옴
    }

    // 세부 메시지만 반환
    public String getDetailMessage() {
        return detailMessage;
    }


}

