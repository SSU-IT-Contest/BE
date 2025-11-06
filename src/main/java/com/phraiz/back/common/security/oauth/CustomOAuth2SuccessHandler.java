package com.phraiz.back.common.security.oauth;

import com.phraiz.back.common.security.jwt.JwtUtil;
import com.phraiz.back.member.dto.response.oauth.CustomOAuth2User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String,String> redisTemplate;

    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        CustomOAuth2User customOAuth2User= (CustomOAuth2User) authentication.getPrincipal();
        String id = customOAuth2User.getEmail().substring(0, customOAuth2User.getEmail().indexOf("@"));

        System.out.println("로그인 유저: " + customOAuth2User);  // 로그 확인
        System.out.println("유저 ID: " + id); // 네이버/카카오 모두 찍히는지

        // 토큰 발급 //
        // 토큰 생성
        //String accessToken = jwtUtil.generateAccessToken(id);
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

        // 임시 토큰 만들기
        String tempToken = UUID.randomUUID().toString();
        // Redis에 저장 (userId → tempCode or tempCode → userId)
        redisTemplate.opsForValue().set(tempToken, id, Duration.ofMinutes(5));

//        String redirectUrl = "https://ssu-phraiz-fe.vercel.app/oauth2/callback" +
//                "?accessToken=" + accessToken;

        String redirectUrl="https://www.phraiz.com/login/oauth?tempToken=" + tempToken;
        response.sendRedirect(redirectUrl);

        // 쿠키, 타임리프, 엔드포인트를 하나 만들기
        // cors 설정

//        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}

