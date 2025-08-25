package com.phraiz.back.cite.domain;

import com.phraiz.back.common.domain.BaseFolder;
import com.phraiz.back.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cite_folder")
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CiteFolder extends BaseFolder {

}
