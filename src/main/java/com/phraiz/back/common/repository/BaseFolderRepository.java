package com.phraiz.back.common.repository;

import com.phraiz.back.common.domain.BaseFolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;

@NoRepositoryBean
public interface BaseFolderRepository<T extends BaseFolder>
        extends JpaRepository<T, Long> {
    Page<T> findAllByMemberId(String memberId, Pageable pageable);
    Optional<T> findByIdAndMemberId(Long id, String memberId);
    boolean existsByMemberIdAndName(String memberId, String name);
}
