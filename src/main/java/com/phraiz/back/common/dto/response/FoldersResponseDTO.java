package com.phraiz.back.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
public class FoldersResponseDTO {
    private List<Folders> folders;

    @AllArgsConstructor
    @Data
    @Builder
    public static class Folders {
        private Long id;
        private String name;
        private LocalDateTime createdAt;
    }
}


