package com.phraiz.back.cite.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
// 사용자에게 보여줄 정보만 포함
public class CitationResponseDTO {
    private Long citeId;         // 고유 식별자 (클릭 시 상세 보기 등)
    private String title;        // 논문 제목 (또는 "제목 없음")
    private String style;        // 인용 스타일 (APA, MLA 등)
    private String citation;     // 생성된 인용문 텍스트
    private String url;          // 원본 논문 URL
    private LocalDateTime createdAt; // 생성 시각 (정렬/표시용)

    private String cslJson;
}
