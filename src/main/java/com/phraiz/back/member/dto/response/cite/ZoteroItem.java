package com.phraiz.back.member.dto.response.cite;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// 인용 정보
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ZoteroItem {
    private String itemType;
    private String title;
    private List<Creator> creators;
    private String publicationTitle;
    private String date;
    private String DOI;
    private String url;


}
