package com.phraiz.back.member.service;

import com.phraiz.back.member.dto.response.cite.ZoteroItem;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class CiteTranslationService {
   private final RestTemplate restTemplate;

   public CiteTranslationService() {
       this.restTemplate = new RestTemplate();
   }
    public ZoteroItem translateFromUrl(String url) {
        String zoteroUrl = "http://localhost:1969/web";

        // 요청 헤더 구성
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 요청 본문 구성
        Map<String, String> body = new HashMap<>();
        body.put("url", url);
        body.put("session", UUID.randomUUID().toString()); // 또는 고정된 세션 문자열 사용 가능

        HttpEntity<Map<String,String>> entity = new HttpEntity<>(body, headers);

        // zoterUrl에 POST로 요청 보내고 응답 받음
        ResponseEntity<ZoteroItem[]> response = restTemplate.postForEntity(zoteroUrl, entity, ZoteroItem[].class);

        ZoteroItem[] items = response.getBody();
        if (items != null && items.length > 0) {
            return items[0]; // 첫 번째 item 반환
        } else {
            throw new RuntimeException("Zotero translation failed or returned empty result.");
        }

        //Map<String, String> body = Map.of("url", url);
        //HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        //ResponseEntity<ZoteroItem[]> response = restTemplate.postForEntity(endpoint, request, ZoteroItem[].class);

        //return response.getBody()[0]; // 첫 번째 아이템만 사용
    }

}
