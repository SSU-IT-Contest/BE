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
import com.phraiz.back.summary.domain.SummaryContent;
import com.phraiz.back.summary.domain.SummaryHistory;
import com.phraiz.back.summary.dto.response.SummaryResponseDTO;
import com.phraiz.back.summary.exception.SummaryErrorCode;
import com.phraiz.back.summary.repository.SummaryContentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;


@Service
@Transactional
@Slf4j
public class SummaryHistoryService extends AbstractHistoryService<SummaryHistory> {

    private final MemberRepository memberRepository;
    private final SummaryContentRepository summaryContentRepository;

    private final int MAX_HISTORY_FOR_FREE = 30;
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyMMdd");

    protected SummaryHistoryService(BaseHistoryRepository<SummaryHistory> repo, 
                                   MemberRepository memberRepository,
                                   SummaryContentRepository summaryContentRepository) {
        super(repo);
        this.memberRepository = memberRepository;
        this.summaryContentRepository = summaryContentRepository;
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
            long currentCount = repo.countByMemberId(memberId);

            if (currentCount >= MAX_HISTORY_FOR_FREE) {
                throw new BusinessLogicException(SummaryErrorCode.PLAN_LIMIN_EXCEEDED);
            }
        }
    }

    // 새로운 히스토리 생성 메서드 (SummaryService에서 사용)
    public SummaryHistory createNewHistory(String memberId, Long folderId) {
        // 1. 히스토리 개수 검증
        validateRemainingHistoryCount(memberId);
        
        // 2. 임시 제목으로 히스토리 생성
        SummaryHistory newHistory = SummaryHistory.builder()
                .memberId(memberId)
                .folderId(folderId)
                .name("temp")
                .build();
        
        repo.saveAndFlush(newHistory);
        
        // 3. 실제 제목 생성 및 설정
        String finalTitle = buildDatedTitle("요약", newHistory.getId());
        newHistory.setName(finalTitle);
        
        return newHistory;
    }

    // 히스토리 content 조회 (sequenceNumber 지정 가능, null이면 최신 조회)
    public SummaryResponseDTO readHistoryContent(String memberId, Long historyId, Integer sequenceNumber) {
        // 1. 히스토리 존재 및 권한 확인
        SummaryHistory history = repo.findByIdAndMemberId(historyId, memberId)
                .orElseThrow(() -> new BusinessLogicException(SummaryErrorCode.HISTORY_NOT_FOUND));
        
        // 2. Content 조회
        SummaryContent content;
        if (sequenceNumber != null) {
            // 특정 sequence number의 content 조회
            content = summaryContentRepository.findByHistoryIdAndSequenceNumber(historyId, sequenceNumber)
                    .orElseThrow(() -> new BusinessLogicException(SummaryErrorCode.CONTENT_NOT_FOUND));
        } else {
            // 최신 content 조회
            content = summaryContentRepository.findLatestByHistoryId(historyId)
                    .orElseThrow(() -> new BusinessLogicException(SummaryErrorCode.CONTENT_NOT_FOUND));
        }
        
        // 3. DTO 생성 및 반환
        return SummaryResponseDTO.builder()
                .historyId(history.getId())
                .name(history.getName())
                .originalText(content.getOriginalText())
                .summarizedText(content.getSummarizedText())
                .sequenceNumber(content.getSequenceNumber())
                .mode(content.getMode())
                .target(content.getTarget())
                .question(content.getQuestion())
                .remainingToken(0L)  // 조회 시에는 토큰 정보 불필요
                .build();
    }

    // 기존 saveOrUpdateHistory는 호환성을 위해 남겨둠 (필요시 삭제 가능)
    @Deprecated
    public HistoryMetaDTO saveOrUpdateHistory(String memberId,
                                              Long folderId,
                                              Long historyId,
                                              String content ) {
        if (historyId != null) {
            SummaryHistory history = repo.findByIdAndMemberId(historyId, memberId)
                    .orElseThrow(() -> new EntityNotFoundException("히스토리를 찾을 수 없습니다."));
            return new HistoryMetaDTO(history.getId(), history.getName(), null);
        }

        SummaryHistory newHistory = createNewHistory(memberId, folderId);
        return new HistoryMetaDTO(newHistory.getId(), newHistory.getName(), null);
    }

    private String buildDatedTitle(String label, long id) {
        String today = LocalDate.now(ZoneId.of("Asia/Seoul")).format(DATE_FMT);
        return today + "-" + label + "-" + id;
    }

}
