package com.phraiz.back.common.dto.request;

public record HistoryUpdateDTO(
        String name,
        Long   folderId   // 새 폴더 ID (null이면 이동 안 함)
) {
}
