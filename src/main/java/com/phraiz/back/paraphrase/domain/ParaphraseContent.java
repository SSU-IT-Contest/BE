package com.phraiz.back.paraphrase.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "paraphrase_contents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ParaphraseContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "history_id", nullable = false)
    private ParaphraseHistory history;

    @Lob
    @Column(name = "original_text", columnDefinition = "TEXT", nullable = false)
    private String originalText;

    @Lob
    @Column(name = "paraphrased_text", columnDefinition = "TEXT", nullable = false)
    private String paraphrasedText;

    @Column(name = "sequence_number", nullable = false)
    private Integer sequenceNumber;

    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public ParaphraseContent(ParaphraseHistory history, String originalText, 
                            String paraphrasedText, Integer sequenceNumber) {
        this.history = history;
        this.originalText = originalText;
        this.paraphrasedText = paraphrasedText;
        this.sequenceNumber = sequenceNumber;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
