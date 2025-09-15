package com.phraiz.back.cite.service;

import com.phraiz.back.cite.domain.Cite;
import com.phraiz.back.cite.domain.CiteFolder;
import com.phraiz.back.cite.dto.request.CitationRequestDTO;
import com.phraiz.back.cite.dto.request.RenameRequestDTO;
import com.phraiz.back.cite.dto.response.CitationResponseDTO;
import com.phraiz.back.cite.dto.response.FolderResponseDTO;
import com.phraiz.back.cite.exception.CiteErrorCode;
import com.phraiz.back.cite.repository.CiteRepository;
import com.phraiz.back.common.dto.response.HistoryMetaDTO;
import com.phraiz.back.common.exception.custom.BusinessLogicException;
import com.phraiz.back.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class CiteService {
    private final CiteRepository citeRepository;
    private final CiteHistoryService citeHistoryService;

    // 0. 소유권 체크(본인만 수정)
    public void checkCiteOwnership(Long memberId, Long citeOwnerId){
        if (!memberId.equals(citeOwnerId)) {
            throw new BusinessLogicException(CiteErrorCode.NO_PERMISSION_TO_UPDATE);
        }

    }
    // 1. 인용문 저장 과정
    // 1-1. 처음에 요청받은 url과 변환된 csl json 저장
    // 식별자 리턴
    public Long saveCslJson(String cslJson, String url, Member member) {
        Cite cite = Cite.builder()
                .member(member)
                .cslJson(cslJson)
                .title("제목없음")
                .url(url)
                .build();
        Cite result=citeRepository.save(cite);
        return result.getCiteId();
    }

    // 1-2. 인용문과 스타일 저장
    @Transactional
    public void saveCitation(String memberId, CitationRequestDTO citationRequestDTO) {
        Cite cite = citeRepository.findById(citationRequestDTO.getCiteId())
                .orElseThrow(() -> new BusinessLogicException(CiteErrorCode.CITE_NOT_FOUND));

        cite.setCitation(citationRequestDTO.getCitation());
        cite.setStyle(citationRequestDTO.getStyle());

        String result = citationRequestDTO.getCitation();
        Long citeId = cite.getCiteId();
        Long folderId = citationRequestDTO.getFolderId();
        Long historyId = citationRequestDTO.getHistoryId();

        // 내용 저장 (히스토리 업데이트)
        HistoryMetaDTO metaDTO = citeHistoryService.saveOrUpdateHistory(  // ★
                memberId,
                folderId,      // 루트면 null
                historyId,
                result,      // content
                cite
        );
    }

    // 1-3. 인용문 조회
    public CitationResponseDTO getCiteDetail(Member member, Long citeId) {
        Cite cite = citeRepository.findById(citeId)
                .orElseThrow(() -> new BusinessLogicException(CiteErrorCode.CITE_NOT_FOUND));

        // 소유권 체크 (본인만 조회 가능)
        checkCiteOwnership(member.getMemberId(), cite.getMember().getMemberId());

        // 엔티티 → DTO 변환
        return CitationResponseDTO.builder()
                .citeId(cite.getCiteId())
                .title(cite.getTitle())
                .style(cite.getStyle())
                .citation(cite.getCitation())
                .url(cite.getUrl())
                .createdAt(cite.getCreatedAt())
                .cslJson(cite.getCslJson())
                .build();

    }


}
