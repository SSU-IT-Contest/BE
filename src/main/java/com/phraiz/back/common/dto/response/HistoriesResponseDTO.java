package com.phraiz.back.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
public class HistoriesResponseDTO {

    private List<Histories> histories;

    @AllArgsConstructor
    @Data
    @Builder
    public static class Histories {
        private Long id;
        private String name;
        private LocalDateTime lastUpdate;
    }
}
