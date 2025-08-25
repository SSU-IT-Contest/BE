package com.phraiz.back.cite.dto.request;

import jakarta.validation.constraints.NotNull;

public record CitationUpdateRequestDTO (
        String name,
        @NotNull(message = "citeId는 필수입니다.")
        Long citeId
) {

}
