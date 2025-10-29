package com.phraiz.back.cite.repository;

import com.phraiz.back.cite.domain.CiteContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CiteContentRepository extends JpaRepository<CiteContent, Long> {
    
    /**
     * 특정 히스토리의 특정 sequenceNumber에 해당하는 content 조회
     */
    Optional<CiteContent> findByHistoryIdAndSequenceNumber(Long historyId, Integer sequenceNumber);
    
    /**
     * 특정 히스토리의 가장 최근 content 조회 (sequenceNumber가 가장 큰 것)
     */
    Optional<CiteContent> findFirstByHistoryIdOrderBySequenceNumberDesc(Long historyId);
    
    /**
     * 특정 히스토리의 최대 sequenceNumber 조회
     */
    @Query("SELECT MAX(c.sequenceNumber) FROM CiteContent c WHERE c.history.id = :historyId")
    Optional<Integer> findMaxSequenceNumberByHistoryId(@Param("historyId") Long historyId);
}
