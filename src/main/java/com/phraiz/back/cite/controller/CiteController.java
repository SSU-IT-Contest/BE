package com.phraiz.back.cite.controller;

import com.phraiz.back.cite.domain.CiteFolder;
import com.phraiz.back.cite.dto.request.CitationRequestDTO;
import com.phraiz.back.cite.dto.request.RenameRequestDTO;
import com.phraiz.back.cite.dto.response.CitationResponseDTO;
import com.phraiz.back.cite.dto.response.FolderResponseDTO;
import com.phraiz.back.cite.dto.response.ZoteroItem;
import com.phraiz.back.cite.service.CiteConvertService;
import com.phraiz.back.cite.service.CiteService;
import com.phraiz.back.cite.service.CiteTranslationService;
import com.phraiz.back.common.security.user.CustomUserDetails;
import com.phraiz.back.member.domain.Member;
import lombok.AllArgsConstructor;
import net.minidev.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/cite")
@AllArgsConstructor
public class CiteController {
    private final CiteConvertService citeConvertService;
    private final CiteTranslationService citeTranslationService;
    private final CiteService citeService;

    // 1. 인용문 저장 과정
    // 1-1. 메타데이터 가지고오고 CSL json으로 변환 후 csl, cite_id 응답보내기
    @PostMapping("/getUrlData")
    public ResponseEntity<Map<String, Object>> getUrlData(@RequestBody Map<String, String> request,  @AuthenticationPrincipal CustomUserDetails userDetails) {
        Member member=userDetails.getMember();
        Map<String, Object> response = new HashMap<>();
        String url=request.get("url");

        // 1. URL 을 Zotero Translation Server 에 보내서 논문 등의 메타데이터를 가져옴
        ZoteroItem item = citeTranslationService.translateFromUrl(url);

        // 2. cslJson 으로 변환
        JSONObject cslJson=citeConvertService.toCSL(item);
        String csl=cslJson.toString();

        // 3. cslJson & url 저장
        // 응답으로 식별자도 리턴
        Long citeId=citeService.saveCslJson(csl,url,member);

        response.put("csl", cslJson);
        response.put("citeId",citeId);

        return ResponseEntity.ok(response);
    }

    // 1-2. 인용문 받아서 저장
    @PostMapping("/getCitation")
    public ResponseEntity<Map<String, Object>> getCitation(@RequestBody CitationRequestDTO citationRequestDTO) {

        citeService.saveCitation(citationRequestDTO);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "인용문 저장 완료");
        return ResponseEntity.ok(response);
    }

    // 1-3. 인용문 조회
    @GetMapping("/citeDetail/{citeId}")
    public ResponseEntity<CitationResponseDTO> getCiteDetail(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long citeId) {
        Member member=userDetails.getMember();
        CitationResponseDTO citationResponseDTO = citeService.getCiteDetail(member, citeId);
        return ResponseEntity.ok(citationResponseDTO);
    }


    // 2. 히스토리
    // 2-1. 사용자별 저장된 인용문 리스트 가져오기
    @GetMapping("/myCitations")
    public ResponseEntity<List<Map<String, Object>>> getMyCitations(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Member member=userDetails.getMember();
        List<Map<String, Object>> citationList = citeService.getMyCitations(member);

        return ResponseEntity.ok(citationList);
    }
    // 2-2. 파일 이름 변경
    @PatchMapping("/renameCiteFile")
    public ResponseEntity<?> renameCiteFile(@RequestBody RenameRequestDTO renameRequestDTO,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Member member = userDetails.getMember();
        citeService.renameCiteFile(renameRequestDTO, member);

        return ResponseEntity.ok(Map.of("message", "파일 이름이 성공적으로 변경되었습니다."));
    }

    // 2-3. 파일 삭제
    @DeleteMapping("/deleteCiteFile")
    public ResponseEntity<?> deleteCiteFile(@RequestBody Map<String, Long> request, @AuthenticationPrincipal CustomUserDetails userDetails){
        Member member = userDetails.getMember();
        Long citeId=request.get("citeId");
        citeService.deleteCiteFile(citeId,member);

        return ResponseEntity.ok(Map.of("message", "파일이 성공적으로 삭제되었습니다."));

    }

    // 2-4. 기록 검색
    // 제목

    // 폴더 생성은 Basic 부터
    // 3. 폴더
    // 3-1. 폴더 생성
    @PostMapping("/folder/createFolder")
    public ResponseEntity<Map<String, Object>> createFolder(@RequestBody Map<String, String> request, @AuthenticationPrincipal CustomUserDetails userDetails){
        Member member = userDetails.getMember();
        String folderName = request.get("folderName");

        citeService.createCiteFolder(member,folderName);

        return ResponseEntity.ok(Map.of("message", "폴더가 성공적으로 생성되었습니다."));

    }
    // 3-2. 폴더에 파일 저장
    // 3-3. 사용자별 저장된 폴더 조회
    @GetMapping("/folder/getMyFolders")
    public ResponseEntity<List<FolderResponseDTO>> getMyFolders(@AuthenticationPrincipal CustomUserDetails userDetails) {

        Member member=userDetails.getMember();
        List<FolderResponseDTO> citeFolderList = citeService.getMyFolders(member);

        return ResponseEntity.ok(citeFolderList);
    }
    // 3-4. 사용자별 폴더 속 아이템 조회
    // GET 에는 바디 안 씀
    @GetMapping("/folder/getMyFolderItems")
    public ResponseEntity<List<Map<String, Object>>> getMyFolderItems(@RequestParam("folderId") Long folderId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        Member member=userDetails.getMember();
        List<Map<String, Object>> itemsList = citeService.getMyFolderItems(folderId, member);

        return ResponseEntity.ok(itemsList);
    }
    // 3-5. 아이템 이동
    // 폴더 이름 변경
    // 3-3. 폴더 삭제
    // 3-4. 파일 위치 변경


}
