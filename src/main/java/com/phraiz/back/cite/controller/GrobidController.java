package com.phraiz.back.cite.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.phraiz.back.cite.dto.request.CitationRequestDTO;
import com.phraiz.back.cite.dto.request.GrobidRequestDTO;
import com.phraiz.back.cite.dto.response.ZoteroItem;
import com.phraiz.back.cite.service.CiteConvertService;
import com.phraiz.back.cite.service.GrobidService;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cite")
@Slf4j
public class GrobidController {

    private final GrobidService grobidService;
    private final CiteConvertService citeConvertService;

    public GrobidController(GrobidService grobidService, CiteConvertService citeConvertService) {
        this.grobidService = grobidService;
        this.citeConvertService = citeConvertService;
    }

    @PostMapping("/grobid")
    public ResponseEntity<Map<String, Object>> convertCitation(@RequestBody GrobidRequestDTO grobidRequestDTO) throws Exception {
        Map<String, Object> response = new HashMap<>();
        String citation = grobidRequestDTO.getCitation();
        log.info("[convertCitation] grobid 호출 시작");
        JsonNode grobidJson = grobidService.parseCitation(citation); // grobid 호출
        log.info("[convertCitation] grobid 호출 완료");
        ZoteroItem zoteroItem = grobidService.grobidJsonToZoteroItem(grobidJson); // grobid 호출된 값 -> zoteroItem으로 변환
        log.info("[convertCitation] grobid 데이터 -> zoteroItem 으로 변환 완료");
        JSONObject jsonObject = citeConvertService.toCSL(zoteroItem); // csl json 로 변환
        log.info("[convertCitation] zoteroItem -> csl json 으로 변환 완료");

        response.put("cslJson", jsonObject);

        return ResponseEntity.ok(response);
    }
}
