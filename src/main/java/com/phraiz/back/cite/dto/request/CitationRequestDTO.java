package com.phraiz.back.cite.dto.request;

import lombok.Data;

@Data
public class CitationRequestDTO {
    private Long citeId;
    private String citation;
    // private String title;
    private String style;
    private String url;

    Long folderId;
    Long historyId;
}
