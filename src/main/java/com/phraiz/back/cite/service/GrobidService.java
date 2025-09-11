package com.phraiz.back.cite.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phraiz.back.cite.dto.response.Creator;
import com.phraiz.back.cite.dto.response.ZoteroItem;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class GrobidService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String grobidUrl = "http://localhost:8070/api/processCitation";


    // GROBID 호출
    public JsonNode parseCitation(String citation) throws Exception {

        if (citation == null || citation.isEmpty()) {
            throw new IllegalArgumentException("인용문(citation)이 비어있습니다.");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        //String body = "citation=" + citation + "&consolidate=true";
        String body = "citation=" + URLEncoder.encode(citation, StandardCharsets.UTF_8) + "&consolidate=true";

        System.out.println("body: " + body);
        HttpEntity<String> request = new HttpEntity<>(body, headers);

        //String response = restTemplate.postForObject(grobidUrl, request, String.class);
        try {
            // ⭐ exchange() 메서드로 변경하여 전체 응답 객체(ResponseEntity)를 받습니다.
            ResponseEntity<String> response = restTemplate.exchange(
                    grobidUrl,
                    HttpMethod.POST,
                    request, // HttpEntity 객체
                    String.class
            );
            // ⭐ 상태 코드 확인
            System.out.println("GROBID HTTP Status Code: " + response.getStatusCode());
            // ⭐ 응답 바디가 존재하는지 확인
            String responseBody = response.getBody();
            if (responseBody == null || responseBody.isEmpty()) {
                throw new Exception("GROBID server returned an empty or null body.");
            }

            System.out.println("GROBID response = " + responseBody);

        //System.out.println("GROBID response = " + response); // 실제 XML 확인

        // TEI XML을 JSON으로 변환 (간단히 구조화)
        // 실제로는 TEI XML 파서 이용 가능
        //JsonNode jsonNode = objectMapper.readTree(response);
            JsonNode jsonNode = objectMapper.readTree(responseBody);
        return jsonNode;
        } catch (Exception e) {
            // RestTemplate은 4xx, 5xx 에러 발생 시 Client/ServerException을 던집니다.
            System.err.println("Error during GROBID API call: " + e.getMessage());
            throw e; // 예외를 다시 던져 컨트롤러에서 처리
        }
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
