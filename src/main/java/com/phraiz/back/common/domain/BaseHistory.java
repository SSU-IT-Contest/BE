package com.phraiz.back.common.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@MappedSuperclass                     // ← 도메인별 히스토리 엔티티가 상속
@Data
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString(callSuper = true)
public abstract class BaseHistory{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                 // 1. PK

    @Column(name = "member_id", nullable = false)
    private String memberId;         // 2. 소유 회원 식별자

    @Column(nullable = false)
    private String name;             // 3. 히스토리 이름

    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt; // 4. 생성 시각

    /* ---------- FK 컬럼 ---------- */
    @Column(name = "folder_id", nullable = true)
    private Long folderId;  // 계층이 0 계층인 history의 경우, null

    @Lob
    //@Column(nullable = true)
    @Column(name = "content", columnDefinition = "LONGTEXT")
    private String content;          // 5. 본문(대용량 텍스트 가능)

    @Column(name = "last_update", nullable = false)
    private LocalDateTime lastUpdate; // 6. 최종 수정 시각

    /* 수정 시 lastUpdate 자동 갱신 */
    @PreUpdate
    protected void onUpdate() {
        this.lastUpdate = LocalDateTime.now();
    }

    /* 생성 시 createdAt 자동 세팅 */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.lastUpdate = LocalDateTime.now();
    }


}
