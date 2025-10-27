package com.phraiz.back.cite.controller;

import com.phraiz.back.cite.dto.request.CitationRequestDTO;
import com.phraiz.back.cite.dto.response.CitationHistoryContentResponseDTO;
import com.phraiz.back.cite.dto.response.CitationResponseDTO;
import com.phraiz.back.cite.dto.response.Creator;
import com.phraiz.back.cite.dto.response.ZoteroItem;
import com.phraiz.back.cite.exception.CiteErrorCode;
import com.phraiz.back.cite.parser.DbpiaAuthorParser;
import com.phraiz.back.cite.parser.KissAuthorParser;
import com.phraiz.back.cite.parser.KyoboAuthorParser;
import com.phraiz.back.cite.service.*;
import com.phraiz.back.common.dto.request.HistoryUpdateDTO;
import com.phraiz.back.common.dto.request.UpdateRequestDTO;
import com.phraiz.back.common.dto.response.FoldersResponseDTO;
import com.phraiz.back.common.dto.response.HistoriesResponseDTO;
import com.phraiz.back.common.exception.custom.BusinessLogicException;
import com.phraiz.back.common.security.user.CustomUserDetails;
import com.phraiz.back.common.util.SecurityUtil;
import com.phraiz.back.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/cite")
@AllArgsConstructor
@Slf4j
public class CiteController {
    private final CiteConvertService citeConvertService;
    private final CiteTranslationService citeTranslationService;
    private final CiteService citeService;
    private final CiteHistoryService citeHistoryService;
    private final CiteFolderService citeFolderService;

    /* ---------- 1. 인용문 생성 과정 ---------- */
    // 1. 인용문 저장 과정
    // 1-1. 메타데이터 가지고오고 CSL json 으로 변환 후 csl, cite_id 응답보내기
    @PostMapping("/getUrlData")
    public ResponseEntity<Map<String, Object>> getUrlData(@RequestBody Map<String, String> request,  @AuthenticationPrincipal CustomUserDetails userDetails) {
        Member member=userDetails.getMember();
        Map<String, Object> response = new HashMap<>();
        String url=request.get("url");

        // 1. URL 을 Zotero Translation Server 에 보내서 논문 등의 메타데이터를 가져옴
        log.info("[getUrlData] Zotero Translation Server 호출 시작");
        ZoteroItem item = citeTranslationService.translateFromUrl(url);
        log.info("[getUrlData] Zotero Translation Server 응답 완료: item_title={}", item.getTitle());

        // author null 확인
        if (item.getCreators() == null || item.getCreators().isEmpty() ||
                (item.getCreators().get(0).getLastName() == null && item.getCreators().get(0).getFirstName() == null)) {
            log.info("[getUrlData] metadata author==null");
            List<Creator> creators = null;
            if (url.contains("dbpia.co.kr")) {
                // DbpiaAuthorParser 호출
                creators = DbpiaAuthorParser.getAuthor(url);
                item.setCreators(creators);
            } else if (url.contains("kiss.kstudy.com")) {
                // KissAuthorParser 호출
                creators = KissAuthorParser.getAuthor(url);
                item.setCreators(creators);
            }else if (url.contains("scholar.kyobobook.co.kr")) {
                // KyoboAuthorParser 호출
                creators = KyoboAuthorParser.getAuthors(url);
                item.setCreators(creators);
            }
            // 저자를 계속 찾지 못하면 예외를 발생
            if (creators == null || creators.isEmpty()) {
                throw new BusinessLogicException(CiteErrorCode.METADATA_EXTRACTION_FAILED);
            }
           // item.setCreators(creators);

        }
        // 2. cslJson 으로 변환
        log.info("[getUrlData] CSL 변환 시작");
        JSONObject cslJson=citeConvertService.toCSL(item);
        String csl=cslJson.toString();
        log.info("[getUrlData] CSL 변환 완료");

        // 3. cslJson & url 저장
        // 응답으로 식별자도 리턴
        Long citeId=citeService.saveCslJson(csl,url,member);
        log.info("[getUrlData] DB 저장 완료: citeId={}", citeId);

        response.put("csl", cslJson);
        response.put("citeId",citeId);

        return ResponseEntity.ok(response);
    }

    // 1-2. 인용문 받아서 저장
    @PostMapping("/getCitation")
    public ResponseEntity<Map<String, Object>> getCitation(@RequestBody CitationRequestDTO citationRequestDTO) {
        String memberId = SecurityUtil.getCurrentMemberId();
        citeService.saveCitation(memberId, citationRequestDTO);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "인용문 저장 완료");
        log.info("[getCitation] 인용문 저장 완료");
        return ResponseEntity.ok(response);
    }

    // 1-3. 인용문 조회
    @GetMapping("/citeDetail/{citeId}")
    public ResponseEntity<CitationResponseDTO> getCiteDetail(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long citeId) {
        Member member=userDetails.getMember();
        CitationResponseDTO citationResponseDTO = citeService.getCiteDetail(member, citeId);
        return ResponseEntity.ok(citationResponseDTO);
    }

    /* ---------- 2. 폴더 ---------- */

    // 2-1. 폴더 목록 (page,size optional)
    @GetMapping("/folders")
    public Page<FoldersResponseDTO> getFolders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        String memberId = SecurityUtil.getCurrentMemberId();
        return citeFolderService.getFolders(memberId, page, size);
    }

    // 2-2. 폴더 생성
    @PostMapping("/folders")
    public ResponseEntity<Void> createFolder(@RequestBody UpdateRequestDTO dto) {
        String memberId = SecurityUtil.getCurrentMemberId();
        citeFolderService.createFolder(memberId, dto.name());
        return ResponseEntity.ok().build();
    }

    // 2-3. 폴더 이름 수정
    @PatchMapping("/folders/{folderId}")
    public ResponseEntity<Void> renameFolder(@PathVariable Long folderId,
                                             @RequestBody UpdateRequestDTO dto) {
        String memberId = SecurityUtil.getCurrentMemberId();
        citeFolderService.renameFolder(memberId, folderId, dto.name());
        return ResponseEntity.ok().build();
    }

    // 2-4. 폴더 삭제
    @DeleteMapping("/folders/{folderId}")
    public ResponseEntity<Void> deleteFolder(@PathVariable Long folderId) {
        String memberId = SecurityUtil.getCurrentMemberId();
        citeFolderService.deleteFolder(memberId, folderId);
        return ResponseEntity.ok().build();
    }


    /* ---------- 3. 히스토리 ---------- */

    // 3-1. 히스토리 목록 (page,size optional)
    @GetMapping("/histories")
    public Page<HistoriesResponseDTO> getHistories(@RequestParam(required = false) Long folderId,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "5") int size) {

        String memberId = SecurityUtil.getCurrentMemberId();
        return citeHistoryService.getHistories(memberId, folderId, page, size);
    }

    // 3-2. 히스토리 생성
    @PostMapping("/histories")
    public ResponseEntity<Void> createHistory(@RequestParam(required = false) Long folderId,
                                              @RequestBody UpdateRequestDTO dto) {
        String memberId = SecurityUtil.getCurrentMemberId();
        citeHistoryService.createHistory(memberId, folderId, dto.name());
        return ResponseEntity.ok().build();
    }

    // 3-3. 히스토리 이동 및 이름 수정(폴더 변경) -- PATCH 하나로 처리
    @PatchMapping("/histories/{historyId}")
    public ResponseEntity<Void> updateHistory(@PathVariable Long historyId,
                                              @RequestBody HistoryUpdateDTO dto) {
        String memberId = SecurityUtil.getCurrentMemberId();
        citeHistoryService.updateHistory(memberId, historyId, dto);
        return ResponseEntity.ok().build();
    }

    // 3-4. 히스토리 삭제
    @DeleteMapping("/histories/{historyId}")
    public ResponseEntity<Void> deleteHistory(@PathVariable Long historyId) {
        String memberId = SecurityUtil.getCurrentMemberId();
        citeHistoryService.deleteHistory(memberId, historyId);
        return ResponseEntity.ok().build();
    }

    // 3-5. 히스토리 특정 content 조회 (sequenceNumber로 조회, 없으면 최신 조회)
    @GetMapping("/histories/{historyId}")
    public CitationHistoryContentResponseDTO getHistoryContent(
            @PathVariable Long historyId,
            @RequestParam(required = false) Integer sequenceNumber) {
        String memberId = SecurityUtil.getCurrentMemberId();
        return citeHistoryService.readCitationHistoryContent(memberId, historyId, sequenceNumber);
    }
}
