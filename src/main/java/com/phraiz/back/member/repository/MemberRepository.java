package com.phraiz.back.member.repository;

import com.phraiz.back.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsById(String id);
    boolean existsByEmail(String email);

    Optional<Member> findById(String id);

    Optional<Member> findByEmail(String email);

    // 엔티티 필드 변경 없이 Repository 에 추가
    Optional<Member> findByMemberId(Long memberId); // 식별자
}
