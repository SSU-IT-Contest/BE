package com.phraiz.back.summary.controller;

import com.phraiz.back.common.security.user.CustomUserDetails;
import com.phraiz.back.common.util.SecurityUtil;
import com.phraiz.back.member.domain.Member;
import com.phraiz.back.paraphrase.dto.request.ParaphraseRequestDTO;
import com.phraiz.back.paraphrase.dto.response.ParaphraseResponseDTO;
import com.phraiz.back.paraphrase.service.ParaphraseService;
import com.phraiz.back.summary.dto.request.SummaryRequestDTO;
import com.phraiz.back.summary.dto.response.SummaryResponseDTO;
import com.phraiz.back.summary.service.SummaryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/summary")
public class SummaryController {

    /*
        AI 요약 구현
        1. 모드 별 요약
        2. 요약 폴더 목록 불러오기
        3. 히스토리 목록 불러오기
         - 매 요청마다?(캐싱 포함) 또는 처음에 싹 다?
        4. 히스토리 이동 요청
     */

    private final SummaryService summaryService;

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


}
