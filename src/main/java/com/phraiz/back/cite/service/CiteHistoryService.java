package com.phraiz.back.cite.service;

import com.phraiz.back.cite.domain.Cite;
import com.phraiz.back.cite.domain.CiteHistory;
import com.phraiz.back.cite.dto.response.CitationHistoryContentResponseDTO;
import com.phraiz.back.cite.exception.CiteErrorCode;
import com.phraiz.back.cite.repository.CiteRepository;
import com.phraiz.back.common.dto.response.HistoriesResponseDTO;
import com.phraiz.back.common.dto.response.HistoryMetaDTO;
import com.phraiz.back.common.enums.Plan;
import com.phraiz.back.common.exception.custom.BusinessLogicException;
import com.phraiz.back.common.repository.BaseHistoryRepository;
import com.phraiz.back.common.service.AbstractHistoryService;
import com.phraiz.back.member.domain.Member;
import com.phraiz.back.member.exception.MemberErrorCode;
import com.phraiz.back.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.Collections;

@Service
@Transactional
@Slf4j
public class CiteHistoryService extends AbstractHistoryService<CiteHistory> {

    private final MemberRepository memberRepository;
    private final CiteRepository citeRepository;

    private final int MAX_HISTORY_FOR_FREE = 30;

    protected CiteHistoryService(BaseHistoryRepository<CiteHistory> repo, MemberRepository memberRepository, CiteRepository citeRepository) {
        super(repo);
        this.memberRepository = memberRepository;
        this.citeRepository = citeRepository;
    }

    @Override
    protected HistoriesResponseDTO toDTO(CiteHistory entity) {
        HistoriesResponseDTO.Histories historyItem = HistoriesResponseDTO.Histories.builder()
                .id(entity.getId())
                .name(entity.getName())
                .lastUpdate(entity.getLastUpdate())
                .build();

        return HistoriesResponseDTO.builder()
                .histories(Collections.singletonList(historyItem))
                .build();
    }

    @Override
    protected CiteHistory newHistoryEntity(String memberId, Long folderId, String name) {
        return CiteHistory.builder()
                .memberId(memberId)
                .folderId(folderId)
                .name(name)
                .build();
    }

    @Override
    protected void validateRemainingHistoryCount(String memberId) {
        Member member=memberRepository.findById(memberId).orElseThrow(()->new BusinessLogicException(MemberErrorCode.USER_NOT_FOUND));
        Plan userPlan = Plan.fromId(member.getPlanId());
        if(userPlan == Plan.FREE){
            // 히스토리 개수 불러오기. 30개 이하인 경우, true return.
            long currentCount = repo.countByMemberId(memberId);   // ← 여기!

            if (currentCount >= MAX_HISTORY_FOR_FREE) {
                throw new BusinessLogicException(CiteErrorCode.PLAN_LIMIT_EXCEEDED);
            }
        }
    }

    public HistoryMetaDTO saveOrUpdateHistory(String memberId,
                                              Long folderId,      // 루트면 null
                                              Long historyId,
                                              String content, Cite cite ) {
        // 1) UPDATE
        if (historyId != null) {
            CiteHistory history = repo.findByIdAndMemberId(historyId, memberId)
                    .orElseThrow(() -> new EntityNotFoundException("히스토리를 찾을 수 없습니다."));
            history.setContent(content);
            return new HistoryMetaDTO(history.getId(), history.getName());
        }

        // 2) CREATE
        //String autoTitle = makeDefaultTitle(content);      // 본문 앞 30자 + "..." 등
        // 원하는 형식: yyMMdd
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd");

        String autoTitle = cite.getCreatedAt().format(formatter) + "-" + "인용-" + cite.getCiteId();
        CiteHistory newHistory = CiteHistory.builder()
                .memberId(memberId)
                .folderId(folderId)
                .name(autoTitle)
                .content(content)
                .cite(cite)
                .build();

        repo.save(newHistory);
        return new HistoryMetaDTO(newHistory.getId(), newHistory.getName());
    }

    private String makeDefaultTitle(String text) {
        return (text.length() > 30 ? text.substring(0, 30) + "…" : text);
    }

    // ⭐ 새로운 인용문 히스토리를 생성하는 메서드 (외부 호출용)
    public void createCitationHistory(String memberId, Long folderId, String content, Long citeId) {
        validateRemainingHistoryCount(memberId);

        // citeId로 Cite 엔티티를 조회합니다.
        Cite cite = citeRepository.findById(citeId)
                .orElseThrow(() -> new BusinessLogicException(CiteErrorCode.CITE_NOT_FOUND));

        String autoTitle = makeDefaultTitle(content);

        CiteHistory newHistory = CiteHistory.builder()
                .memberId(memberId)
                .folderId(folderId)
                .name(autoTitle)
                .content(content)
                .cite(cite) // ✅ Cite 객체로 관계 설정
                .build();

        repo.save(newHistory);
        new HistoryMetaDTO(newHistory.getId(), newHistory.getName());

    }

    public CitationHistoryContentResponseDTO readCitationHistoryContent(String memberId, Long id) {
        // 1. 부모 클래스처럼 히스토리 엔티티를 찾습니다.
        CiteHistory history = repo.findByIdAndMemberId(id, memberId)
                .orElseThrow(() -> new EntityNotFoundException("히스토리를 찾을 수 없습니다."));

        // 2. CiteHistory 엔티티에서 cite 객체에 접근하여 citeId를 가져옵니다.
        // getCite()는 JPA 연관관계 매핑을 통해 Cite 엔티티를 반환합니다.
        Long citeId = history.getCite().getCiteId();

        // 3. 빌더에 citeId를 추가하여 DTO 를 반환합니다.
        return CitationHistoryContentResponseDTO.builder()
                .id(history.getId())
                .content(history.getContent())
                .lastUpdate(history.getLastUpdate())
              //  .citeId(citeId) // ⭐ citeId를 추가
                .build();
    }

}
