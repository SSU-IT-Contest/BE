package com.phraiz.back.summary.controller;

import com.phraiz.back.common.util.SecurityUtil;
import com.phraiz.back.common.dto.request.HistoryUpdateDTO;
import com.phraiz.back.summary.dto.request.SummaryRequestDTO;
import com.phraiz.back.common.dto.request.UpdateRequestDTO;
import com.phraiz.back.common.dto.response.FoldersResponseDTO;
import com.phraiz.back.common.dto.response.HistoriesResponseDTO;
import com.phraiz.back.common.dto.response.HistoryContentResponseDTO;
import com.phraiz.back.summary.dto.response.SummaryResponseDTO;
import com.phraiz.back.summary.service.SummaryFolderService;
import com.phraiz.back.summary.service.SummaryHistoryService;
import com.phraiz.back.summary.service.SummaryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/summary")
public class SummaryController {

    /*
        AI 요약 구현
        1. 모드 별 요약
        2.폴더 관련 기능
            2-1. 폴더 목록 조회
            2-2. 폴더 생성
            2-3. 폴더 수정(이름)
            2-4. 폴더 삭제
        3. 히스토리 관련 기능
            3-1. 히스토리 목록 조회
             - 매 요청마다?(캐싱 포함) 또는 처음에 싹 다?
            3-2. 히스토리 생성
            3-3. 히스토리 이동 및 이름 수정(폴더 변경)
            3-4. 히스토리 삭제
            3-5. 선택한 히스토리의 최근 기록 조회
        조회 기능은 페이지네이션을 적용.
     */

    private final SummaryService summaryService;
    private final SummaryFolderService summaryFolderService;
    private final SummaryHistoryService summaryHistoryService;

    // 1. 모드 별 요약
    // 1-1. 한 줄 요약
    @PostMapping("/summarize/one-line")
    public ResponseEntity<?> oneLineSummary(HttpServletRequest request, HttpServletResponse response,
                                            @RequestBody SummaryRequestDTO dto) {
        String memberId = SecurityUtil.getCurrentMemberId();
        SummaryResponseDTO result = summaryService.oneLineSummary(memberId, dto);
        return ResponseEntity.ok(result);
    }

    // 1-2. 전체 요약
    @PostMapping("/summarize/full")
    public ResponseEntity<?> fullSummary(HttpServletRequest request, HttpServletResponse response,
                                         @RequestBody SummaryRequestDTO dto) {
        String memberId = SecurityUtil.getCurrentMemberId();
        SummaryResponseDTO result = summaryService.fullSummary(memberId, dto);
        return ResponseEntity.ok(result);
    }

    // 1-3. 문단 별 요약
    @PostMapping("/summarize/by-paragraph")
    public ResponseEntity<?> paragraphSummary(HttpServletRequest request, HttpServletResponse response,
                                              @RequestBody SummaryRequestDTO dto) {
        String memberId = SecurityUtil.getCurrentMemberId();
        SummaryResponseDTO result = summaryService.paragraphSummary(memberId, dto);
        return ResponseEntity.ok(result);
    }

    // 1-4. 핵심 요약
    @PostMapping("/summarize/key-points")
    public ResponseEntity<?> keyPointSummary(HttpServletRequest request, HttpServletResponse response,
                                             @RequestBody SummaryRequestDTO dto) {
        String memberId = SecurityUtil.getCurrentMemberId();
        SummaryResponseDTO result = summaryService.keyPointSummary(memberId, dto);
        return ResponseEntity.ok(result);
    }

    // 1-5. 질문 기반 요약
    @PostMapping("/summarize/question-based")
    public ResponseEntity<?> questionBasedSummary(HttpServletRequest request, HttpServletResponse response,
                                                  @RequestBody SummaryRequestDTO dto) {
        String memberId = SecurityUtil.getCurrentMemberId();
        SummaryResponseDTO result = summaryService.questionBasedSummary(memberId, dto);
        return ResponseEntity.ok(result);
    }

    // 1-6. 타겟 요약
    @PostMapping("/summarize/targeted")
    public ResponseEntity<?> targetedSummary(HttpServletRequest request, HttpServletResponse response,
                                             @RequestBody SummaryRequestDTO dto) {
        String memberId = SecurityUtil.getCurrentMemberId();
        SummaryResponseDTO result = summaryService.targetedSummary(memberId, dto);
        return ResponseEntity.ok(result);
    }

    /* ---------- 2. 폴더 ---------- */

    // 2-1. 폴더 목록 (page,size optional)
    @GetMapping("/folders")
    public Page<FoldersResponseDTO> getFolders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        String memberId = SecurityUtil.getCurrentMemberId();
        return summaryFolderService.getFolders(memberId, page, size);
    }

    // 2-2. 폴더 생성
    @PostMapping("/folders")
    public ResponseEntity<Void> createFolder(@RequestBody UpdateRequestDTO dto) {
        String memberId = SecurityUtil.getCurrentMemberId();
        summaryFolderService.createFolder(memberId, dto.name());
        return ResponseEntity.ok().build();
    }

    // 2-3. 폴더 이름 수정
    @PatchMapping("/folders/{folderId}")
    public ResponseEntity<Void> renameFolder(@PathVariable Long folderId,
                                             @RequestBody UpdateRequestDTO dto) {
        String memberId = SecurityUtil.getCurrentMemberId();
        summaryFolderService.renameFolder(memberId, folderId, dto.name());
        return ResponseEntity.ok().build();
    }

    // 2-4. 폴더 삭제
    @DeleteMapping("/folders/{folderId}")
    public ResponseEntity<Void> deleteFolder(@PathVariable Long folderId) {
        String memberId = SecurityUtil.getCurrentMemberId();
        summaryFolderService.deleteFolder(memberId, folderId);
        return ResponseEntity.ok().build();
    }

    /* ---------- 3. 히스토리 ---------- */

    // 3-1. 히스토리 목록 (page,size optional)
    @GetMapping("/histories")
    public Page<HistoriesResponseDTO> getHistories(@RequestParam(required = false) Long folderId,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "5") int size) {

        String memberId = SecurityUtil.getCurrentMemberId();
        return summaryHistoryService.getHistories(memberId, folderId, page, size);
    }

    // 3-2. 히스토리 생성
    @PostMapping("/histories")
    public ResponseEntity<Void> createHistory(@RequestParam(required = false) Long folderId,
                                              @RequestBody UpdateRequestDTO dto) {
        String memberId = SecurityUtil.getCurrentMemberId();
        summaryHistoryService.createHistory(memberId, folderId, dto.name());
        return ResponseEntity.ok().build();
    }

    // 3-3. 히스토리 이동 및 이름 수정(폴더 변경) -- PATCH 하나로 처리
    @PatchMapping("/histories/{historyId}")
    public ResponseEntity<Void> updateHistory(@PathVariable Long historyId,
                                              @RequestBody HistoryUpdateDTO dto) {
        String memberId = SecurityUtil.getCurrentMemberId();
        summaryHistoryService.updateHistory(memberId, historyId, dto);
        return ResponseEntity.ok().build();
    }

    // 3-4. 히스토리 삭제
    @DeleteMapping("/histories/{historyId}")
    public ResponseEntity<Void> deleteHistory(@PathVariable Long historyId) {
        String memberId = SecurityUtil.getCurrentMemberId();
        summaryHistoryService.deleteHistory(memberId, historyId);
        return ResponseEntity.ok().build();
    }

    // 3-5. 히스토리 특정 content 조회 (sequenceNumber로 조회, 없으면 최신 조회)
    @GetMapping("/histories/{historyId}")
    public SummaryResponseDTO getHistoryContent(
            @PathVariable Long historyId,
            @RequestParam(required = false) Integer sequenceNumber) {
        String memberId = SecurityUtil.getCurrentMemberId();
        return summaryHistoryService.readHistoryContent(memberId, historyId, sequenceNumber);
    }




    /* ---------- 파일 업로드 ---------- */
    @PostMapping("/file-upload")
    public ResponseEntity<?> extractTextFromPdf(@RequestPart("file") MultipartFile file,
                                                @RequestParam("mode") String mode,
                                                @RequestParam(value = "target", required = false) String target,
                                                @RequestParam(value = "question", required = false) String question,
                                                @RequestParam(value = "historyId", required = false) Long historyId,
                                                @RequestParam(value = "folderId", required = false) Long folderId) {
        String memberId = SecurityUtil.getCurrentMemberId();
        SummaryResponseDTO result = summaryService.uploadFile(memberId, file, mode, target, question, historyId, folderId);
        return ResponseEntity.ok(result);
    }
}
