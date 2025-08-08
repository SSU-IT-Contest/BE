package com.phraiz.back.cite.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.phraiz.back.common.exception.ErrorCode;
import lombok.Getter;

@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum CiteErrorCode implements ErrorCode {

    // 1. 인용문 관련
    CITE_NOT_FOUND(404, "CIT001", "존재하지 않는 인용입니다.", "CITATION"),
    NO_PERMISSION_TO_UPDATE(403, "CIT002", "인용 수정 권한이 없습니다.", "CITATION"),

    // 2. 인용 폴더 관련
    FOLDER_NOT_FOUND(404, "CIT002", "존재하지 않는 폴더입니다.", "CITATION");


    private final int status;
    private final String code;
    private final String message;
    private final String service;

    CiteErrorCode(int status, String code, String message, String service) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.service = service;
    }
}