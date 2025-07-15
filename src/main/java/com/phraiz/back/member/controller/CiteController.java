package com.phraiz.back.member.controller;

import com.phraiz.back.member.dto.response.cite.ZoteroItem;
import com.phraiz.back.member.service.CiteConvertService;
import com.phraiz.back.member.service.CiteService;
import com.phraiz.back.member.service.CiteTranslationService;
import lombok.AllArgsConstructor;
import net.minidev.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

//👉 Zotero API 활용: URL만 던지면, 메타데이터 자동 수집
// url 의 메타데이터를 가지고 오려면 zotero translation server 사용해야 함
//👉 Zotero는 책, 논문, 웹페이지 구분 없이 잘 처리함
//
//👉 결과 받아서 포맷 생성 (APA/MLA)
//
//비용: 무료, 다만 설정 약간 필요 (OAuth or API 토큰)

@RestController
@RequestMapping("/api/cite")
@AllArgsConstructor
public class CiteController {
    private final CiteConvertService citeConvertService;
    private final CiteService citeService;
    private final CiteTranslationService citeTranslationService;

    // 메타데이터 가지고오기
    @PostMapping("/getUrlData")
    public ResponseEntity<Map<String, Object>> getCite(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        String url=request.get("url");
        String style=request.get("style");

        // 1. URL을 Zotero Translation Server에 보내서 논문 등의 메타데이터를 가져옴
        ZoteroItem item = citeTranslationService.translateFromUrl(url);

//        response.put("title", item.getTitle());
//        response.put("type", item.getItemType());
//        response.put("url", item.getUrl());
//        response.put("date", item.getDate());
//        response.put("publicationTitle", item.getPublicationTitle());

        // 2. Zotero Web API에 아이템 저장 (또는 로컬에서 처리)
        // Zotero API 서버에 POST 요청을 보내고, 생성된 itemKey를 받아옴
        //String itemKey = citeService.createItem(item);

        // 3. 인용문 생성
        //String citation = citeService.getCitation(itemKey, style);

        // [메타데이터] + [스타일] => CSL Processor => [인용문 / 참고문헌 텍스트]
        // 1. cslJson은 변환
        JSONObject cslJson=citeConvertService.toCSL(item);
        // 2. cls Processor 사용
        String citation=citeService.generateCite(style, cslJson);
        System.out.println("citation:"+citation);

        response.put("citation", citation);

        return ResponseEntity.ok(response);
    }
}
