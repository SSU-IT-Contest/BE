package com.phraiz.back.member.domain;

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
    @Column(nullable = false)
    private String style;
    @Column(nullable = false)
    private String url;
    @Column(nullable = false)
    private String title;
    @Column(nullable = false)
    private String citation;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;







}
