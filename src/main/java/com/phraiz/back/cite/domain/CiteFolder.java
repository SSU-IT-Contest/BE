package com.phraiz.back.cite.domain;

import com.phraiz.back.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cite_folder")
@Data
@ToString
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor
@Builder
public class CiteFolder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "folder_id", updatable = false)
    private Long folderId; // 인용생성폴더 테이블 식별자

    private String name; // 폴더 이름

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;// 멤버 식별자

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "parent_folder_id")
//    private Folder parentFolder; // 상위 폴더(최상위면 null)

//    @OneToMany(mappedBy = "parentFolder")
//    private List<Folder> subFolders = new ArrayList<>();

    @OneToMany(mappedBy = "folder")
    private List<CiteFolderItem> files = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
