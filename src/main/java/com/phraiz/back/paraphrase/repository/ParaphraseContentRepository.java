package com.phraiz.back.paraphrase.repository;

import com.phraiz.back.paraphrase.domain.ParaphraseContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ParaphraseContentRepository extends JpaRepository<ParaphraseContent, Long> {
    
    // 특정 history의 모든 contents 조회 (최신 순)
    @Query("SELECT pc FROM ParaphraseContent pc WHERE pc.history.id = :historyId ORDER BY pc.sequenceNumber DESC")
    List<ParaphraseContent> findByHistoryIdOrderBySequenceNumberDesc(@Param("historyId") Long historyId);
    
    // 특정 history의 특정 sequence content 조회
    Optional<ParaphraseContent> findByHistoryIdAndSequenceNumber(Long historyId, Integer sequenceNumber);
    
    // 특정 history의 최신 content 조회
    @Query("SELECT pc FROM ParaphraseContent pc WHERE pc.history.id = :historyId ORDER BY pc.sequenceNumber DESC LIMIT 1")
    Optional<ParaphraseContent> findLatestByHistoryId(@Param("historyId") Long historyId);
    
    // 특정 history의 content 개수
    @Query("SELECT COUNT(pc) FROM ParaphraseContent pc WHERE pc.history.id = :historyId")
    Long countByHistoryId(@Param("historyId") Long historyId);
}
