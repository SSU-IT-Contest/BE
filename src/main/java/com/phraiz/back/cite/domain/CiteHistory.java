package com.phraiz.back.cite.domain;

import com.phraiz.back.common.domain.BaseHistory;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cite_history")
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class CiteHistory extends BaseHistory {

    // citeId 대신 Cite 엔티티 자체를 필드로 선언
    @ManyToOne(fetch = FetchType.LAZY) // 다대일 관계 (다수의 히스토리가 하나의 인용문을 가짐)
    @JoinColumn(name = "cite_id", nullable = false)
    private Cite cite; // Cite 엔티티를 참조하는 필드
    
    @OneToMany(mappedBy = "history", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequenceNumber DESC")
    @Builder.Default
    private List<CiteContent> contents = new ArrayList<>();
    
    // Content 추가 메서드
    public void addContent(CiteContent content) {
        this.contents.add(content);
    }
    
    // 최대 10개로 제한하면서 오래된 content 제거
    public void limitContentsToTen() {
        if (this.contents.size() > 10) {
            List<CiteContent> toRemove = this.contents.subList(10, this.contents.size());
            toRemove.clear();
        }
    }
}
