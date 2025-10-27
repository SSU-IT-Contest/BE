package com.phraiz.back.summary.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "summary_contents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SummaryContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "history_id", nullable = false)
    private SummaryHistory history;

    @Lob
    @Column(name = "original_text", columnDefinition = "TEXT", nullable = false)
    private String originalText;

    @Lob
    @Column(name = "summarized_text", columnDefinition = "TEXT", nullable = false)
    private String summarizedText;

    @Column(name = "sequence_number", nullable = false)
    private Integer sequenceNumber;

    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public SummaryContent(SummaryHistory history, String originalText, 
                         String summarizedText, Integer sequenceNumber) {
        this.history = history;
        this.originalText = originalText;
        this.summarizedText = summarizedText;
        this.sequenceNumber = sequenceNumber;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
