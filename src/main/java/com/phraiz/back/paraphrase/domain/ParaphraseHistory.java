package com.phraiz.back.paraphrase.domain;

import com.phraiz.back.common.domain.BaseHistory;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "paraphrase_history")
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ParaphraseHistory extends BaseHistory {
}