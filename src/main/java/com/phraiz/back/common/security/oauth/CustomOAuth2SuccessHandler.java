package com.phraiz.back.common.security.oauth;

import com.phraiz.back.common.security.jwt.JwtUtil;
import com.phraiz.back.member.dto.response.oauth.CustomOAuth2User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String,String> redisTemplate;

    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        CustomOAuth2User customOAuth2User= (CustomOAuth2User) authentication.getPrincipal();
        String id=customOAuth2User.getUsername();

        System.out.println("로그인 유저: " + customOAuth2User);  // 로그 확인
        System.out.println("유저 ID: " + customOAuth2User.getUsername()); // 네이버/카카오 모두 찍히는지

        // 토큰 발급 //
        // 토큰 생성
        String accessToken = jwtUtil.generateToken(id);
        String refreshToken = jwtUtil.generateRefreshToken(id);

        // refresh token redis에 저장
        redisTemplate.opsForValue().set(
                "RT:"+id,
                refreshToken,
                jwtUtil.getRefreshTokenExpTime(), TimeUnit.MILLISECONDS
        );

        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true); // https 에서만 전송
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge((int) (jwtUtil.getRefreshTokenExpTime() / 1000));

        // TODO
        String redirectUrl = "http://localhost:3000/oauth2/callback" +
                "?accessToken=" + accessToken;

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}

