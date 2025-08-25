package com.phraiz.back.cite.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@AllArgsConstructor
@Data
@Builder
public class CitationHistoryContentResponseDTO {

    private Long id;
    private String content;
    private LocalDateTime lastUpdate;

    private Long citeId;
}
