package com.phraiz.back.cite.service;

import com.phraiz.back.cite.domain.Cite;
import com.phraiz.back.cite.domain.CiteContent;
import com.phraiz.back.cite.domain.CiteHistory;
import com.phraiz.back.cite.dto.response.CitationHistoryContentResponseDTO;
import com.phraiz.back.cite.exception.CiteErrorCode;
import com.phraiz.back.cite.repository.CiteContentRepository;
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
    private final CiteContentRepository citeContentRepository;

    private final int MAX_HISTORY_FOR_FREE = 30;

    protected CiteHistoryService(BaseHistoryRepository<CiteHistory> repo, 
                                 MemberRepository memberRepository, 
                                 CiteRepository citeRepository,
                                 CiteContentRepository citeContentRepository) {
        super(repo);
        this.memberRepository = memberRepository;
        this.citeRepository = citeRepository;
        this.citeContentRepository = citeContentRepository;
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
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessLogicException(MemberErrorCode.USER_NOT_FOUND));
        Plan userPlan = Plan.fromId(member.getPlanId());
        if(userPlan == Plan.FREE){
            long currentCount = repo.countByMemberId(memberId);
            if (currentCount >= MAX_HISTORY_FOR_FREE) {
                throw new BusinessLogicException(CiteErrorCode.PLAN_LIMIT_EXCEEDED);
            }
        }
    }

    /**
     * 새로운 히스토리를 생성하는 메서드 (CiteService에서 사용)
     * ParaphraseHistoryService 구조 참고 - 히스토리 생성만 담당
     */
    public CiteHistory createNewHistory(String memberId, Long folderId, Cite cite) {
        // 1. 히스토리 개수 검증
        validateRemainingHistoryCount(memberId);
        
        // 2. 제목 생성
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd");
        String autoTitle = cite.getCreatedAt().format(formatter) + "-" + "인용-" + cite.getCiteId();
        
        // 3. 히스토리 생성 및 저장
        CiteHistory newHistory = CiteHistory.builder()
                .memberId(memberId)
                .folderId(folderId)
                .name(autoTitle)
                .cite(cite)
                .build();
        
        repo.save(newHistory);
        
        return newHistory;
    }
    
    /**
     * 기존 히스토리에 새로운 content를 추가하는 메서드
     */
    public Integer addContentToHistory(Long historyId, String memberId, String citationText) {
        CiteHistory history = repo.findByIdAndMemberId(historyId, memberId)
                .orElseThrow(() -> new BusinessLogicException(CiteErrorCode.HISTORY_NOT_FOUND));
        
        // 새로운 sequenceNumber 계산 (가장 최근 content의 sequenceNumber + 1)
        Integer nextSeqNum = citeContentRepository.findMaxSequenceNumberByHistoryId(historyId)
                .map(max -> max + 1)
                .orElse(1);
        
        // content 테이블에 새로운 레코드 추가
        CiteContent newContent = CiteContent.builder()
                .history(history)
                .citationText(citationText)
                .sequenceNumber(nextSeqNum)
                .build();
        
        history.addContent(newContent);
        citeContentRepository.save(newContent);
        
        // 10개로 제한
        history.limitContentsToTen();
        
        return nextSeqNum;
    }

    /**
     * 새로운 인용문 히스토리를 생성하고 첨 번째 content를 추가하는 메서드 (외부 호출용)
     * createNewHistory와 addContentToHistory를 결합하여 사용
     */
    public CiteHistory createCitationHistory(String memberId, Long folderId, String citationText, Long citeId) {
        // 1. Cite 조회
        Cite cite = citeRepository.findById(citeId)
                .orElseThrow(() -> new BusinessLogicException(CiteErrorCode.CITE_NOT_FOUND));
        
        // 2. 히스토리 생성
        CiteHistory newHistory = createNewHistory(memberId, folderId, cite);
        
        // 3. 첨 번째 content 추가
        CiteContent firstContent = CiteContent.builder()
                .history(newHistory)
                .citationText(citationText)
                .sequenceNumber(1)
                .build();
        
        newHistory.addContent(firstContent);
        citeContentRepository.save(firstContent);
        
        return newHistory;
    }

    /**
     * 특정 sequenceNumber의 content를 조회하는 메서드
     */
    public CitationHistoryContentResponseDTO readCitationHistoryContent(String memberId, Long historyId, Integer sequenceNumber) {
        CiteHistory history = repo.findByIdAndMemberId(historyId, memberId)
                .orElseThrow(() -> new BusinessLogicException(CiteErrorCode.HISTORY_NOT_FOUND));

        // sequenceNumber가 제공되면 해당 content를 조회, 아니면 가장 최근 content 조회
        CiteContent content;
        if (sequenceNumber != null) {
            content = citeContentRepository.findByHistoryIdAndSequenceNumber(historyId, sequenceNumber)
                    .orElseThrow(() -> new BusinessLogicException(CiteErrorCode.CONTENT_NOT_FOUND));
        } else {
            content = citeContentRepository.findFirstByHistoryIdOrderBySequenceNumberDesc(historyId)
                    .orElseThrow(() -> new BusinessLogicException(CiteErrorCode.CONTENT_NOT_FOUND));
        }

        return CitationHistoryContentResponseDTO.builder()
                .id(history.getId())
                .citationText(content.getCitationText())
                .sequenceNumber(content.getSequenceNumber())
                .lastUpdate(history.getLastUpdate())
                .build();
    }
}
