package com.phraiz.back.member.dto.request;

import com.phraiz.back.member.domain.Member;
import com.phraiz.back.member.enums.LoginType;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SignUpRequestDTO {
    @Pattern(regexp = "^[A-Za-z0-9]{4,15}$",
            message = "아이디는 영문 또는 숫자 조합의 4~15자여야 합니다."
    )
    private String id;
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+=-]).{8,20}$",
            message = "비밀번호는 영문, 숫자, 특수문자를 포함한 8~20자여야 합니다."
    )
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
