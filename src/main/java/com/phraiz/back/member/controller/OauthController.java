package com.phraiz.back.member.controller;

import com.phraiz.back.common.security.jwt.JwtUtil;
import com.phraiz.back.member.dto.response.LoginResponseDTO;
import com.phraiz.back.member.service.MemberService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/oauth")
@RequiredArgsConstructor
public class OauthController {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    private final JwtUtil jwtUtil;
    private final MemberService memberService;

    @PostMapping("/token")
    public ResponseEntity<?> exchangeTempCode(@RequestBody Map<String, String> request, HttpServletResponse response) {
        String tempToken = request.get("code");

        String id = redisTemplate.opsForValue().get(tempToken);
        if (id == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired code");
        }

        // Redis에서 임시 토큰 제거 (1회용)
        redisTemplate.delete(tempToken);

        // Access/Refresh Token 생성
        String accessToken = jwtUtil.generateAccessToken(id);
        String refreshToken = jwtUtil.generateRefreshToken(id);

        // refresh token redis에 저장
        redisTemplate.opsForValue().set(
                "RT:"+id,
                refreshToken,
                jwtUtil.getRefreshTokenExpTime(), TimeUnit.MILLISECONDS
        );

        // refresh token을 httpOnly 쿠키에 저장
        Cookie refreshTokenCookie = memberService.addCookie(refreshToken);

        response.addCookie(refreshTokenCookie);

        // 응답 DTO에서는 refreshToken 제거
        LoginResponseDTO loginResponseDTO =memberService.getMember(id, accessToken);

        return ResponseEntity.ok(loginResponseDTO);
    }

}
