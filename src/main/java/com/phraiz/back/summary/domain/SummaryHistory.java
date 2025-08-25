package com.phraiz.back.summary.domain;

import com.phraiz.back.common.domain.BaseHistory;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "summary_history")
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SummaryHistory extends BaseHistory {
}
