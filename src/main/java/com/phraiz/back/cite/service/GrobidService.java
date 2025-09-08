package com.phraiz.back.cite.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phraiz.back.cite.dto.response.Creator;
import com.phraiz.back.cite.dto.response.ZoteroItem;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class GrobidService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String grobidUrl = "http://localhost:8070/api/processCitation";


    // GROBID 호출
    public JsonNode parseCitation(String citation) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = "citation=" + citation + "&consolidate=true";

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        String response = restTemplate.postForObject(grobidUrl, request, String.class);

        // TEI XML을 JSON으로 변환 (간단히 구조화)
        // 실제로는 TEI XML 파서 이용 가능
        JsonNode jsonNode = objectMapper.readTree(response);
        return jsonNode;
    }

    // GROBID 데이터 -> ZoteroItem 형태로 변환
    public ZoteroItem grobidJsonToZoteroItem(JsonNode grobidJson) {
        ZoteroItem item = new ZoteroItem();
        item.setTitle(grobidJson.path("title").asText());
        item.setPublicationTitle(grobidJson.path("journal").asText());
        item.setDate(grobidJson.path("year").asText());
        item.setDOI(grobidJson.path("DOI").asText(null));
        item.setUrl(grobidJson.path("URL").asText(null));

        // 저자 변환
        List<Creator> creators = new ArrayList<>();
        grobidJson.path("authors").forEach(a -> {
            Creator c = new Creator();
            c.setFirstName(a.path("given").asText());
            c.setLastName(a.path("family").asText());
            creators.add(c);
        });
        item.setCreators(creators);

        item.setItemType("journalArticle"); // 필요에 따라 mapping
        return item;
    }


}
