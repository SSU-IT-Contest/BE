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
    // 인용문 생성 관련 오류 코드 추가
    METADATA_EXTRACTION_FAILED(400, "CIT003", "인용문 메타데이터(저자, 제목 등) 추출에 실패했습니다.", "CITATION"),

    // 2. 인용 폴더 관련
    FOLDER_NOT_FOUND(404, "CIT002", "존재하지 않는 폴더입니다.", "CITATION"),
    
    // 3. 인용 히스토리 관련
    HISTORY_NOT_FOUND(404, "CIT005", "존재하지 않는 인용 히스토리입니다.", "CITATION"),
    CONTENT_NOT_FOUND(404, "CIT006", "존재하지 않는 인용 콘텐츠입니다.", "CITATION"),

    // 4. 요금제 관련
    PLAN_NOT_ACCESSED(400, "CLT003", "무료 요금제 사용자는 이용하실 수 없습니다.", "CITATION"),
    PLAN_LIMIT_EXCEEDED(400, "CLT004", "무료 요금제 제한이 초과되었습니다. 요금제를 업데이트하세요.", "CITATION");

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
