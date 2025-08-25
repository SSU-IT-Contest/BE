package com.phraiz.back.common.repository;


import com.phraiz.back.common.domain.BaseHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;

@NoRepositoryBean
public interface BaseHistoryRepository<T extends BaseHistory>
        extends JpaRepository<T, Long> {
    Page<T> findAllByMemberIdAndFolderId(String memberId, Long folderId, Pageable pageable);
    Page<T> findAllByMemberIdAndFolderIdIsNull(String memberId, Pageable pageable); // folderId가 null인 경우
    Optional<T> findByIdAndMemberId(Long id, String memberId);
    long countByMemberId(String memberId);
}