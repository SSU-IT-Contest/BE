package com.phraiz.back.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@AllArgsConstructor
@Data
@Builder
public class HistoryContentResponseDTO {

    private Long id;
    private String content;
    private LocalDateTime lastUpdate;

}
