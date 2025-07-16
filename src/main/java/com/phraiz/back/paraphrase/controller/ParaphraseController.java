package com.phraiz.back.paraphrase.controller;

import com.phraiz.back.common.security.user.CustomUserDetails;
import com.phraiz.back.common.util.SecurityUtil;
import com.phraiz.back.member.domain.Member;
import com.phraiz.back.paraphrase.dto.request.ParaphraseRequestDTO;
import com.phraiz.back.paraphrase.dto.response.ParaphraseResponseDTO;
import com.phraiz.back.paraphrase.service.ParaphraseService;
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


}
