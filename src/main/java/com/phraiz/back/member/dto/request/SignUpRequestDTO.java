package com.phraiz.back.member.dto.request;

import com.phraiz.back.member.domain.Member;
import com.phraiz.back.member.enums.LoginType;
import lombok.Data;

@Data
public class SignUpRequestDTO {
    private String id;
    private String pwd;
    private String email;
    private LoginType loginType=LoginType.LOCAL;
    private Long planId=1L; // 기본 free

    public Member toEntity() {
        return Member.builder()
                .id(this.id)
                .pwd(this.pwd)
                .email(this.email)
                .loginType(this.loginType)
                .planId(this.planId)
                .build();
    }
}
