package com.phraiz.back.summary.service;

import com.phraiz.back.common.dto.response.HistoryMetaDTO;
import com.phraiz.back.common.exception.custom.BusinessLogicException;
import com.phraiz.back.summary.domain.SummaryContent;
import com.phraiz.back.summary.domain.SummaryHistory;
import com.phraiz.back.summary.exception.SummaryErrorCode;
import com.phraiz.back.summary.repository.SummaryContentRepository;
import com.phraiz.back.summary.repository.SummaryHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SummaryContentService {

    private final SummaryContentRepository summaryContentRepository;
    private final SummaryHistoryRepository summaryHistoryRepository;
    private final SummaryHistoryService summaryHistoryService;

    @Transactional
    public HistoryMetaDTO saveSummaryContent(String memberId, Long folderId, Long historyId,
                                              String originalText, String summarizedText,
                                              String mode, String custom) {
        SummaryHistory history;
        Integer nextSequenceNumber;

        if (historyId != null) {
            // 기존 히스토리에 content 추가
            history = summaryHistoryRepository.findById(historyId)
                    .orElseThrow(() -> new BusinessLogicException(SummaryErrorCode.HISTORY_NOT_FOUND));

            // 현재 content 개수 확인하여 다음 sequence number 계산
            Long contentCount = summaryContentRepository.countByHistoryId(historyId);
            nextSequenceNumber = contentCount.intValue() + 1;

            // 10개 초과 시 가장 오래된 content 삭제
            if (contentCount >= 10) {
                summaryContentRepository.findByHistoryIdOrderBySequenceNumberDesc(historyId)
                        .stream()
                        .skip(9)  // 최신 9개는 유지
                        .forEach(summaryContentRepository::delete);
            }
        } else {
            // 새 히스토리 생성
            history = summaryHistoryService.createNewHistory(memberId, folderId);
            nextSequenceNumber = 1;
        }

        SummaryContent content;

        // Content 생성 및 저장
        SummaryContent.SummaryContentBuilder builder = SummaryContent.builder()
                .history(history)
                .originalText(originalText)
                .summarizedText(summarizedText)
                .sequenceNumber(nextSequenceNumber)
                .mode(mode);

        if ("question-based".equals(mode)) {
            builder.question(custom);
        } else if ("targeted".equals(mode)) {
            builder.target(custom);
        }

        content = builder.build();
        summaryContentRepository.save(content);

        // HistoryMetaDTO 반환
        return new HistoryMetaDTO(history.getId(), history.getName(), nextSequenceNumber);
    }
}
