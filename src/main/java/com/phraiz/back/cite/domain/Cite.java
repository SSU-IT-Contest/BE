package com.phraiz.back.cite.domain;

import com.phraiz.back.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "cite")
@Data
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Cite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cite_id", updatable = false)
    private Long citeId; // 인용생성 테이블 식별자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;// 멤버 식별자

    // Hibernate 는 @Lob + String 을 기본적으로 tinytext 로 매핑
    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String cslJson;  // cslJson으로 변환된 Zotero에서 받은 메타데이터

    @Column(nullable = true)
    private String style;

    @Column(nullable = false)
    private String url;

    @Column(nullable = true)
    private String title;

    @Column(nullable = true)
    private String citation;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }



}
