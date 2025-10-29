package com.phraiz.back.paraphrase.controller;

import com.phraiz.back.common.dto.request.HistoryUpdateDTO;
import com.phraiz.back.common.dto.request.UpdateRequestDTO;
import com.phraiz.back.common.dto.response.FoldersResponseDTO;
import com.phraiz.back.common.dto.response.HistoriesResponseDTO;
import com.phraiz.back.common.dto.response.HistoryContentResponseDTO;
import com.phraiz.back.common.security.user.CustomUserDetails;
import com.phraiz.back.common.util.SecurityUtil;
import com.phraiz.back.member.domain.Member;
import com.phraiz.back.paraphrase.dto.request.ParaphraseRequestDTO;
import com.phraiz.back.paraphrase.dto.response.ParaphraseResponseDTO;
import com.phraiz.back.paraphrase.service.ParaphraseFolderService;
import com.phraiz.back.paraphrase.service.ParaphraseHistoryService;
import com.phraiz.back.paraphrase.service.ParaphraseService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/paraphrase")
public class ParaphraseController {

    /*
        AI 패러프레이징 구현
        1. 모드 별 패러프레이징
        2. 패러프레이징 폴더 목록 불러오기
        3. 히스토리 목록 불러오기
         - 매 요청마다?(캐싱 포함) 또는 처음에 싹 다?
        4. 히스토리 이동 요청
     */

    private final ParaphraseService paraphraseService;
    private final ParaphraseHistoryService paraphraseHistoryService;
    private final ParaphraseFolderService paraphraseFolderService;

    // 1. 모드 별 패러프레이징
    // 1-1. 표준 모드
    @PostMapping("/paraphrasing/standard")
    public ResponseEntity<?> paraphraseStandard(HttpServletRequest request, HttpServletResponse response,
                                                @RequestBody ParaphraseRequestDTO dto) {
        // 로그인한 유저의 ID
        String memberId = SecurityUtil.getCurrentMemberId();
        //String memberId = "user01";
        ParaphraseResponseDTO result = paraphraseService.paraphraseStandard(memberId, dto);
        return ResponseEntity.ok(result);
    }

    // 1-2. 학술적 모드
    @PostMapping("/paraphrasing/academic")
    public ResponseEntity<?> paraphraseAcademic(HttpServletRequest request, HttpServletResponse response,
                                                @RequestBody ParaphraseRequestDTO dto) {
        String memberId = SecurityUtil.getCurrentMemberId();
        ParaphraseResponseDTO result = paraphraseService.paraphraseAcademic(memberId, dto);
        return ResponseEntity.ok(result);
    }

    // 1-3. 창의적 모드
    @PostMapping("/paraphrasing/creative")
    public ResponseEntity<?> paraphraseCreative(HttpServletRequest request, HttpServletResponse response,
                                                @RequestBody ParaphraseRequestDTO dto) {
        String memberId = SecurityUtil.getCurrentMemberId();
        ParaphraseResponseDTO result = paraphraseService.paraphraseCreative(memberId, dto);
        return ResponseEntity.ok(result);
    }

    // 1-4. 유창한 모드
    @PostMapping("/paraphrasing/fluency")
    public ResponseEntity<?> paraphraseFluency(HttpServletRequest request, HttpServletResponse response,
                                               @RequestBody ParaphraseRequestDTO dto) {
        String memberId = SecurityUtil.getCurrentMemberId();
        ParaphraseResponseDTO result = paraphraseService.paraphraseFluency(memberId, dto);
        return ResponseEntity.ok(result);
    }

    // 1-5. 실험적 모드
    @PostMapping("/paraphrasing/experimental")
    public ResponseEntity<?> paraphraseExperimental(HttpServletRequest request, HttpServletResponse response,
                                                    @RequestBody ParaphraseRequestDTO dto) {
        String memberId = SecurityUtil.getCurrentMemberId();
        ParaphraseResponseDTO result = paraphraseService.paraphraseExperimental(memberId, dto);
        return ResponseEntity.ok(result);
    }

    // 1-6. 사용자 지정 모드
    @PostMapping("/paraphrasing/custom")
    public ResponseEntity<?> paraphraseCustom(HttpServletRequest request, HttpServletResponse response,
                                              @RequestBody ParaphraseRequestDTO dto) {
        String memberId = SecurityUtil.getCurrentMemberId();
        ParaphraseResponseDTO result = paraphraseService.paraphraseCustom(memberId, dto);
        return ResponseEntity.ok(result);
    }


    /* ---------- 2. 폴더 ---------- */

    // 2-1. 폴더 목록 (page,size optional)
    @GetMapping("/folders")
    public Page<FoldersResponseDTO> getFolders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        String memberId = SecurityUtil.getCurrentMemberId();
        return paraphraseFolderService.getFolders(memberId, page, size);
    }

    // 2-2. 폴더 생성
    @PostMapping("/folders")
    public ResponseEntity<Void> createFolder(@RequestBody UpdateRequestDTO dto) {
        String memberId = SecurityUtil.getCurrentMemberId();
        paraphraseFolderService.createFolder(memberId, dto.name());
        return ResponseEntity.ok().build();
    }

    // 2-3. 폴더 이름 수정
    @PatchMapping("/folders/{folderId}")
    public ResponseEntity<Void> renameFolder(@PathVariable Long folderId,
                                             @RequestBody UpdateRequestDTO dto) {
        String memberId = SecurityUtil.getCurrentMemberId();
        paraphraseFolderService.renameFolder(memberId, folderId, dto.name());
        return ResponseEntity.ok().build();
    }

    // 2-4. 폴더 삭제
    @DeleteMapping("/folders/{folderId}")
    public ResponseEntity<Void> deleteFolder(@PathVariable Long folderId) {
        String memberId = SecurityUtil.getCurrentMemberId();
        paraphraseFolderService.deleteFolder(memberId, folderId);
        return ResponseEntity.ok().build();
    }

    /* ---------- 3. 히스토리 ---------- */

    // 3-1. 히스토리 목록 (page,size optional)
    @GetMapping("/histories")
    public Page<HistoriesResponseDTO> getHistories(@RequestParam(required = false) Long folderId,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "5") int size) {

        String memberId = SecurityUtil.getCurrentMemberId();
        return paraphraseHistoryService.getHistories(memberId, folderId, page, size);
    }

    // 3-2. 히스토리 생성
    @PostMapping("/histories")
    public ResponseEntity<Void> createHistory(@RequestParam(required = false) Long folderId,
                                              @RequestBody UpdateRequestDTO dto) {
        String memberId = SecurityUtil.getCurrentMemberId();
        paraphraseHistoryService.createHistory(memberId, folderId, dto.name());
        return ResponseEntity.ok().build();
    }

    // 3-3. 히스토리 이동 및 이름 수정(폴더 변경) -- PATCH 하나로 처리
    @PatchMapping("/histories/{historyId}")
    public ResponseEntity<Void> updateHistory(@PathVariable Long historyId,
                                              @RequestBody HistoryUpdateDTO dto) {
        String memberId = SecurityUtil.getCurrentMemberId();
        paraphraseHistoryService.updateHistory(memberId, historyId, dto);
        return ResponseEntity.ok().build();
    }

    // 3-4. 히스토리 삭제
    @DeleteMapping("/histories/{historyId}")
    public ResponseEntity<Void> deleteHistory(@PathVariable Long historyId) {
        String memberId = SecurityUtil.getCurrentMemberId();
        paraphraseHistoryService.deleteHistory(memberId, historyId);
        return ResponseEntity.ok().build();
    }

    // 3-5. 히스토리 특정 content 조회 (sequenceNumber로 조회, 없으면 최신 조회)
    @GetMapping("/histories/{historyId}")
    public ParaphraseResponseDTO getHistoryContent(
            @PathVariable Long historyId,
            @RequestParam(required = false) Integer sequenceNumber) {
        String memberId = SecurityUtil.getCurrentMemberId();
        return paraphraseHistoryService.readHistoryContent(memberId, historyId, sequenceNumber);
    }


}
