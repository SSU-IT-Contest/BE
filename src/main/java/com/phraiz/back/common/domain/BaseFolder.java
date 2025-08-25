package com.phraiz.back.common.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@MappedSuperclass                     // ← 도메인별 엔티티가 상속
@Data
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public abstract class BaseFolder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                 // 1. PK

    @Column(name = "member_id", nullable = false)
    private String memberId;         // 2. 소유 회원 식별자

    @Column(nullable = false)
    private String name;             // 3. 폴더 이름

    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt; // 4. 생성 시각

    /* 생성 시 createdAt 자동 세팅 */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
