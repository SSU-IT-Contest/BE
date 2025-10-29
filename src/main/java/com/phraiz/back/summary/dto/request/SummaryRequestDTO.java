package com.phraiz.back.summary.dto.request;

import lombok.Builder;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Builder
public class SummaryRequestDTO {
        Long folderId;
        Long historyId;
        String text;    // 패러프레이징 요청 text
        String target;  // 타깃 요약 시 필요
        String question;        // 질문 요약 시 필요

}
