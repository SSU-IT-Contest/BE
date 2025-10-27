package com.phraiz.back.cite.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "cite_contents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CiteContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "history_id", nullable = false)
    private CiteHistory history;

    @Lob
    @Column(name = "citation_text", columnDefinition = "LONGTEXT", nullable = false)
    private String citationText;  // 변환된 인용문 텍스트

    @Column(name = "sequence_number", nullable = false)
    private Integer sequenceNumber;

    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public CiteContent(CiteHistory history, String citationText, Integer sequenceNumber) {
        this.history = history;
        this.citationText = citationText;
        this.sequenceNumber = sequenceNumber;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
