package com.phraiz.back.paraphrase.domain;

import com.phraiz.back.common.domain.BaseFolder;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "paraphrase_folder")
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ParaphraseFolder extends BaseFolder {
}