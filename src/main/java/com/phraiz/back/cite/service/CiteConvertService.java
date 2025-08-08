package com.phraiz.back.cite.service;

import com.phraiz.back.cite.dto.response.Creator;
import com.phraiz.back.cite.dto.response.ZoteroItem;
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
        cslJson.put("id", generateUniqueId(zoteroItem));
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


    private JSONArray convertAuthors(List<Creator> creators) {
        JSONArray authors = new JSONArray();
        for (Creator creator : creators) {
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
    private String generateUniqueId(ZoteroItem zoteroItem) {
            // URL이 있으면 URL 해시 사용
            if (zoteroItem.getUrl() != null) {
                return "url-" + Math.abs(zoteroItem.getUrl().hashCode());
            }

            // 3. 제목 + 저자 사용
            String combined = zoteroItem.getTitle() + zoteroItem.getCreators().get(0).getFirstName() + zoteroItem.getCreators().get(0).getLastName();
            return "item-" + Math.abs(combined.hashCode());

    }
}


