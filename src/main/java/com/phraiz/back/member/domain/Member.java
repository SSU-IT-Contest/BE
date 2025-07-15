package com.phraiz.back.member.domain;

import com.phraiz.back.member.enums.LoginType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Optional;

@Entity
@Table(name = "member")
@Data
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id", updatable = false)
    private Long memberId; // 멤버 식별자

    @Column(nullable = false)
    private Long planId;
    // 기본값 0 : free?

    @Column(nullable = false, unique = true)
    private String id;

    private String pwd;

    @Column(unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoginType loginType;

    @Column(nullable = false)
    private LocalDateTime registerDate;

    @Column(nullable = false)
    private String role;


    @PrePersist
    public void prePersist() {
        this.registerDate = LocalDateTime.now();
        if(this.role == null) {
            this.role = "ROLE_USER";
        }
    }


}
