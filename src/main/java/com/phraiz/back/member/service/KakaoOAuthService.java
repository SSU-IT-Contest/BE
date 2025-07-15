package com.phraiz.back.member.service;

import com.phraiz.back.common.security.jwt.JwtUtil;
import com.phraiz.back.member.domain.Member;
import com.phraiz.back.member.dto.response.LoginResponseDTO;
import com.phraiz.back.member.enums.LoginType;
import com.phraiz.back.member.repository.MemberRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class KakaoOAuthService {

    //private final RestTemplate restTemplate;
    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;

//    public LoginResponseDTO processOAuth(String code) {
//        // 1. 인가 코드로 액세스 토큰 요청
//        String tokenUrl = "https://kauth.kakao.com/oauth/token";
//
//        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
//        params.add("grant_type", "authorization_code");
//        params.add("client_id", "{카카오 REST API 키}");
//        params.add("redirect_uri", "https://your-backend.com/oauth/kakao/callback");
//        params.add("code", code);
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//
//        HttpEntity<?> request = new HttpEntity<>(params, headers);
//        ResponseEntity<Map> response = new RestTemplate().postForEntity(tokenUrl, request, Map.class);
//
//        String accessToken = (String) response.getBody().get("access_token");
//
//        // 2. 액세스 토큰으로 사용자 정보 조회
//        HttpHeaders userInfoHeaders = new HttpHeaders();
//        userInfoHeaders.setBearerAuth(accessToken);
//
//        HttpEntity<?> userInfoRequest = new HttpEntity<>(userInfoHeaders);
//        ResponseEntity<Map> userInfoResponse = new RestTemplate()
//                .exchange("https://kapi.kakao.com/v2/user/me", HttpMethod.GET, userInfoRequest, Map.class);
//
//        Map<String, Object> kakaoAccount = (Map<String, Object>) ((Map) userInfoResponse.getBody()).get("kakao_account");
//        String email = (String) kakaoAccount.get("email");
//        String id = String.valueOf(userInfoResponse.getBody().get("id"));
//
//        // 3. 회원 등록 or 로그인 처리
//        Member member = findOrCreateMember(email, id);
//        String newAccessToken = jwtUtil.generateToken(member.getId());
//                String newRefreshToken = jwtUtil.generateRefreshToken(member.getId());
//
//        // 4. Redis, 쿠키 저장 등 처리 (생략 가능)
//        redisTemplate.opsForValue().set("RT:" + member.getId(), newRefreshToken, jwtUtil.getRefreshTokenExpTime(), TimeUnit.MILLISECONDS);
//
//        return new LoginResponseDTO(newAccessToken, member.getId(), member.getRole());
//    }

    private Member findOrCreateMember(String email, String id) {
        return memberRepository.findById(id).orElseGet(() -> {
            Member newMember = Member.builder()
                    .id(id)
                    .email(email)
                    .loginType(LoginType.KAKAO)
                    .planId(1L)
                    .role("USER")
                    .build();
            return memberRepository.save(newMember);
        });
    }
    }
