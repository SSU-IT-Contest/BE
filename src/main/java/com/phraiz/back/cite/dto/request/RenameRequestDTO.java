package com.phraiz.back.cite.dto.request;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class RenameRequestDTO {
    Long citeId;
    String newTitle;
}
