package com.phraiz.back.cite.domain;

import com.phraiz.back.common.domain.BaseHistory;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "cite_history")
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter  // setter 추가
public class CiteHistory extends BaseHistory {

    // citeId 대신 Cite 엔티티 자체를 필드로 선언
    @ManyToOne(fetch = FetchType.LAZY) // 다대일 관계 (다수의 히스토리가 하나의 인용문을 가짐)
    @JoinColumn(name = "cite_id", nullable = false)
    private Cite cite; // Cite 엔티티를 참조하는 필드
    
    // 인용문 내용 (텍스트)
    @Lob
    @Column(nullable = true, columnDefinition = "LONGTEXT")
    private String content;
}
