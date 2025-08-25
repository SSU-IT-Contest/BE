package com.phraiz.back.summary.service;

import com.phraiz.back.common.dto.response.HistoriesResponseDTO;
import com.phraiz.back.common.dto.response.HistoryMetaDTO;
import com.phraiz.back.common.enums.Plan;
import com.phraiz.back.common.exception.custom.BusinessLogicException;
import com.phraiz.back.common.repository.BaseHistoryRepository;
import com.phraiz.back.common.service.AbstractHistoryService;
import com.phraiz.back.member.domain.Member;
import com.phraiz.back.member.exception.MemberErrorCode;
import com.phraiz.back.member.repository.MemberRepository;
import com.phraiz.back.summary.domain.SummaryHistory;
import com.phraiz.back.summary.exception.SummaryErrorCode;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;


@Service
@Transactional
@Slf4j
public class SummaryHistoryService extends AbstractHistoryService<SummaryHistory> {

    private final MemberRepository memberRepository;

    private final int MAX_HISTORY_FOR_FREE = 30;

    protected SummaryHistoryService(BaseHistoryRepository<SummaryHistory> repo, MemberRepository memberRepository) {
        super(repo);
        this.memberRepository = memberRepository;
    }

    @Override
    protected HistoriesResponseDTO toDTO(SummaryHistory entity) {
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
    protected SummaryHistory newHistoryEntity(String memberId, Long folderId, String name) {
        return SummaryHistory.builder()
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
                throw new BusinessLogicException(SummaryErrorCode.PLAN_LIMIN_EXCEEDED);
            }
        }
    }

    public HistoryMetaDTO saveOrUpdateHistory(String memberId,
                                              Long folderId,      // 루트면 null
                                              Long historyId,
                                              String content ) {
        // 1) UPDATE
        if (historyId != null) {
            SummaryHistory history = repo.findByIdAndMemberId(historyId, memberId)
                    .orElseThrow(() -> new EntityNotFoundException("히스토리를 찾을 수 없습니다."));
            history.setContent(content);
            return new HistoryMetaDTO(history.getId(), history.getName());
        }

        // 2) CREATE
        String autoTitle = makeDefaultTitle(content);      // 본문 앞 30자 + "..." 등

        SummaryHistory newHistory = SummaryHistory.builder()
                .memberId(memberId)
                .folderId(folderId)
                .name(autoTitle)
                .content(content)
                .build();

        repo.save(newHistory);
        return new HistoryMetaDTO(newHistory.getId(), newHistory.getName());
    }

    private String makeDefaultTitle(String text) {
        return (text.length() > 30 ? text.substring(0, 30) + "…" : text);
    }

}
