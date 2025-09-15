package com.phraiz.back.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseDTO {
    private String accessToken;
    private Long memberId;
    private String id;
    private String email;
    private String role;

    private Long planId;
}
