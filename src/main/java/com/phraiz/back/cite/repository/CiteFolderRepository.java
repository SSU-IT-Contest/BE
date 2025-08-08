package com.phraiz.back.cite.repository;

import com.phraiz.back.cite.domain.Cite;
import com.phraiz.back.cite.domain.CiteFolder;
import com.phraiz.back.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CiteFolderRepository extends JpaRepository<CiteFolder, Long> {
    List<CiteFolder> findAllByMemberOrderByCreatedAtDesc(Member member);
}
