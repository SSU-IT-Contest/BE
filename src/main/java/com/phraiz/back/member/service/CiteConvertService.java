package com.phraiz.back.member.service;

import com.phraiz.back.member.dto.response.cite.CreatorDTO;
import com.phraiz.back.member.dto.response.cite.ZoteroItem;
import lombok.RequiredArgsConstructor;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CiteConvertService {

    // zoteroItem-> CSL json으로 변환
    public JSONObject toCSL(ZoteroItem zoteroItem) {
        // ZoteroItem → JSONObject (CSL JSON)
        JSONObject cslJson = new JSONObject();
        // 기본 필드 매핑
        cslJson.put("type", convertType(zoteroItem.getItemType()));
        cslJson.put("title", zoteroItem.getTitle());
        cslJson.put("author", convertAuthors(zoteroItem.getCreators()));
        cslJson.put("issued", convertIssuedDate(zoteroItem.getDate()));
        cslJson.put("container-title", zoteroItem.getPublicationTitle());
        cslJson.put("DOI", zoteroItem.getDOI());
        cslJson.put("URL", zoteroItem.getUrl());
        return cslJson;
    }

        private String convertType(String itemType) {
            switch (itemType) {
                case "journalArticle":
                    return "article-journal";
                case "book":
                    return "book";
                case "conferencePaper":
                    return "paper-conference";
                default:
                    return "article"; // fallback
            }
        }


    private JSONArray convertAuthors(List<CreatorDTO> creators) {
        JSONArray authors = new JSONArray();
        for (CreatorDTO creator : creators) {
            JSONObject author = new JSONObject();
            author.put("given", creator.getFirstName());
            author.put("family", creator.getLastName());
            authors.add(author);
        }
        return authors;
    }

    private static JSONObject convertIssuedDate(String zoteroDate) {
        JSONObject issued = new JSONObject();
        JSONArray dateParts = new JSONArray();

        if (zoteroDate != null && zoteroDate.matches("\\d{4}(-\\d{2})?(-\\d{2})?")) {
            String[] parts = zoteroDate.split("-");
            JSONArray dateArray = new JSONArray();
            for (String part : parts) {
                dateArray.add(Integer.valueOf(part));
            }
            dateParts.add(dateArray);
        } else {
            // 기본값 또는 유효하지 않은 날짜 처리
            JSONArray dateArray = new JSONArray();
            dateArray.add(2023); // fallback
            dateParts.add(dateArray);
        }

        issued.put("date-parts", dateParts);
        return issued;
    }
}


