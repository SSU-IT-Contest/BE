package com.phraiz.back.summary.domain;

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
@Table(name = "summary_history")
@SuperBuilder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SummaryHistory extends BaseHistory {
    
    @OneToMany(mappedBy = "history", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequenceNumber DESC")
    @Builder.Default
    private List<SummaryContent> contents = new ArrayList<>();
    
    // Content 추가 메서드
    public void addContent(SummaryContent content) {
        this.contents.add(content);
    }
    
    // 최대 10개로 제한하면서 오래된 content 제거
    public void limitContentsToTen() {
        if (this.contents.size() > 10) {
            List<SummaryContent> toRemove = this.contents.subList(10, this.contents.size());
            toRemove.clear();
        }
    }
}
