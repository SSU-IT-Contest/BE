package com.phraiz.back.paraphrase.service;

import com.phraiz.back.common.dto.response.HistoriesResponseDTO;
import com.phraiz.back.common.enums.Plan;
import com.phraiz.back.common.exception.custom.BusinessLogicException;
import com.phraiz.back.common.repository.BaseHistoryRepository;
import com.phraiz.back.common.service.AbstractHistoryService;
import com.phraiz.back.member.domain.Member;
import com.phraiz.back.member.exception.MemberErrorCode;
import com.phraiz.back.member.repository.MemberRepository;
import com.phraiz.back.paraphrase.domain.ParaphraseHistory;
import com.phraiz.back.common.dto.response.HistoryMetaDTO;
import com.phraiz.back.paraphrase.exception.ParaphraseErrorCode;
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
public class ParaphraseHistoryService extends AbstractHistoryService<ParaphraseHistory> {

    private final MemberRepository memberRepository;

    private final int MAX_HISTORY_FOR_FREE = 30;
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyMMdd"); // 원하는 포맷으로 변경 가능


    protected ParaphraseHistoryService(BaseHistoryRepository<ParaphraseHistory> repo, MemberRepository memberRepository) {
        super(repo);
        this.memberRepository = memberRepository;
    }

    @Override
    protected HistoriesResponseDTO toDTO(ParaphraseHistory entity) {
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
    protected ParaphraseHistory newHistoryEntity(String memberId, Long folderId, String name) {
        return ParaphraseHistory.builder()
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
                throw new BusinessLogicException(ParaphraseErrorCode.PLAN_LIMIN_EXCEEDED);
            }
        }
    }

    public HistoryMetaDTO saveOrUpdateHistory(String memberId,
                                              Long folderId,      // 루트면 null
                                              Long historyId,
                                              String content ) {
        // 1) UPDATE
        if (historyId != null) {
            ParaphraseHistory history = repo.findByIdAndMemberId(historyId, memberId)
                    .orElseThrow(() -> new EntityNotFoundException("히스토리를 찾을 수 없습니다."));
            history.setContent(content);
            return new HistoryMetaDTO(history.getId(), history.getName());
        }

        // 2) CREATE
        // repo.~ 메서드를 통해 ParaphraseHistory 테이블의 가장 큰 historyId 값을 가져오기
        // title = 현재 날짜 + "패러프레이징" + 위에서 가져온 historyId+1
        //String autoTitle = makeDefaultTitle(content);      // 본문 앞 30자 + "..." 등

        // (A) 임시 제목으로 먼저 INSERT 해서 PK(id) 확보
        ParaphraseHistory newHistory = ParaphraseHistory.builder()
                .memberId(memberId)
                .folderId(folderId)
                .name("temp")        // 임시값
                .content(content)
                .build();

        repo.saveAndFlush(newHistory); // <= 여기서 id 생성됨 (IDENTITY일 때)

        // (B) 확정 제목 생성: yyyy-MM-dd 패러프레이징 {id}
        String finalTitle = buildTitle(newHistory.getId());

        // (C) 제목만 수정 -> 트랜잭션 종료 시 UPDATE 1회 (save 호출 불필요)
        newHistory.setName(finalTitle);

        return new HistoryMetaDTO(newHistory.getId(), newHistory.getName());
    }


    private String buildTitle(long id) {
        String today = LocalDate.now(ZoneId.of("Asia/Seoul")).format(DATE_FMT);
        return today + "-패러프레이징-" + id;
    }
//    private String makeDefaultTitle(String text) {
//        return (text.length() > 30 ? text.substring(0, 30) + "…" : text);
//    }

}
