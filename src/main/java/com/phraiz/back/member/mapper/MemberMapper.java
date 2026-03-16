package com.phraiz.back.member.mapper;

import com.phraiz.back.member.domain.Member;
import com.phraiz.back.member.dto.response.LoginResponseDTO;
import com.phraiz.back.member.dto.response.MemberRefDto;
import org.springframework.stereotype.Component;

@Component
public class MemberMapper {

    public LoginResponseDTO toLoginResponseDto(Member member, String accessToken) {
        return new LoginResponseDTO(
                accessToken,
                member.getMemberId(),
                member.getId(),
                member.getEmail(),
                member.getRole(),
                member.getPlanId()
        );
    }

    public MemberRefDto toMemberRefDto(Member member) {
        return new MemberRefDto(
                member.getMemberId(),
                member.getPlanId()
        );
    }
}
