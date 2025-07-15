package com.phraiz.back.member.service;

import com.phraiz.back.common.exception.custom.BusinessLogicException;
import com.phraiz.back.common.security.jwt.JwtUtil;
import com.phraiz.back.member.domain.Member;
import com.phraiz.back.member.dto.response.LoginResponseDTO;
import com.phraiz.back.member.dto.response.oauth.*;
import com.phraiz.back.member.enums.LoginType;
import com.phraiz.back.member.exception.MemberErrorCode;
import com.phraiz.back.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public CustomOAuth2UserService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        System.out.println("oAuth2User: "+oAuth2User.getAttributes());

        String registrationId = userRequest.getClientRegistration().getRegistrationId(); // 어떤 소셜에서 정보 제공했는지
        System.out.println("registrationId = " + registrationId);

        OAuth2Response oAuth2Response=null;

        if (registrationId.equals("naver")){
            oAuth2Response=new NaverResponse(oAuth2User.getAttributes());
        } else if (registrationId.equals("google")) {
            oAuth2Response=new GoogleResponse(oAuth2User.getAttributes());
        }else if (registrationId.equals("kakao")) {
            oAuth2Response=new KakaoResponse(oAuth2User.getAttributes());
        }else{
            return null;
        }

        // 로그인 타입 enum 변환
        LoginType loginType = LoginType.from(registrationId);

        // db에 존재하는지 확인
        String username = oAuth2Response.getProviderId();
        Member existMember=memberRepository.findById(username).orElse(null);
        String role=null;

        Member member;
        if(existMember==null){ // 신규
            // 이메일 중복 확인
            Optional<Member> optionalMember=memberRepository.findByEmail(oAuth2Response.getEmail());
            if(optionalMember.isPresent()){
                throw new BusinessLogicException(MemberErrorCode.DUPLICATE_EMAIL);
            }

            member= Member.builder()
                    .loginType(loginType)
                    .id(oAuth2Response.getProviderId())
                    .email(oAuth2Response.getEmail())
                    .planId(1L)
                    .role(null)
                    .build();
            memberRepository.save(member);
        }else { // 이미 있는 유저
            member=existMember;
            role=existMember.getRole();

        }

        return new CustomOAuth2User(oAuth2Response, role);
    }
}
