//package com.phraiz.back.cite.domain;
//
//import com.phraiz.back.member.domain.Member;
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "cite_folder_item")
//@Data
//@ToString
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
//@AllArgsConstructor
//@Builder
//public class CiteFolderItem {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "item_id", updatable = false)
//    private Long itemId; // 인용생성 폴더아이템 테이블 식별자
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "folder_id")
//    private CiteFolder folder;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "cite_id")
//    private Cite cite;
//
//
//    @Column(name = "created_at", nullable = false)
//    private LocalDateTime createdAt;
//
//    @PrePersist
//    public void prePersist() {
//        this.createdAt = LocalDateTime.now();
//    }
//
//}
