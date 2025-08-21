package com.phraiz.back.summary.domain;

import com.phraiz.back.common.domain.BaseFolder;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "summary_folder")
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SummaryFolder extends BaseFolder {
}
