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
public class FolderResponseDTO {
    private Long folderId;
    private String folderName;
    private LocalDateTime createdAt;
}
