package com.phraiz.back.summary.repository;

import com.phraiz.back.summary.domain.SummaryContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SummaryContentRepository extends JpaRepository<SummaryContent, Long> {
    
    // 특정 history의 모든 contents 조회 (최신 순)
    @Query("SELECT sc FROM SummaryContent sc WHERE sc.history.id = :historyId ORDER BY sc.sequenceNumber DESC")
    List<SummaryContent> findByHistoryIdOrderBySequenceNumberDesc(@Param("historyId") Long historyId);
    
    // 특정 history의 특정 sequence content 조회
    Optional<SummaryContent> findByHistoryIdAndSequenceNumber(Long historyId, Integer sequenceNumber);
    
    // 특정 history의 최신 content 조회
    @Query("SELECT sc FROM SummaryContent sc WHERE sc.history.id = :historyId ORDER BY sc.sequenceNumber DESC LIMIT 1")
    Optional<SummaryContent> findLatestByHistoryId(@Param("historyId") Long historyId);
    
    // 특정 history의 content 개수
    @Query("SELECT COUNT(sc) FROM SummaryContent sc WHERE sc.history.id = :historyId")
    Long countByHistoryId(@Param("historyId") Long historyId);
}
