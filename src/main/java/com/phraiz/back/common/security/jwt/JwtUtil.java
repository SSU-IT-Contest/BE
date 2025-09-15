package com.phraiz.back.common.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Slf4j
@Component
// JWT 토큰 생성하고 검증하고 정보 추출용
// 토큰에 사용자 정보 담아 만들고, 토큰 유효성 검사 및 데이터 추출
public class JwtUtil {
    private final Key key;
    @Getter
    private final Long accessTokenExpTime;
    @Getter
    private final Long refreshTokenExpTime;

    public JwtUtil(
            @Value("${jwt.secret-key}") final String secretKey,
            @Value("${jwt.access-expire}") final long accessTokenExpTime,
            @Value("${jwt.refresh-expire}") final long refreshTokenExpTime)
    {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpTime = accessTokenExpTime;
        this.refreshTokenExpTime = refreshTokenExpTime;
    }

    // access token 생성
    public String generateAccessToken(String id, Long memberId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpTime);

        // 최종적으로 String 의 JWT 토큰 반환
        return Jwts.builder()
                .setSubject(id)
                .claim("memberId", memberId)    // 권한 체크용
                .setIssuedAt(now) // 발급시간
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // refresh token 생성
    public String generateRefreshToken(String id) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpTime);

        return Jwts.builder()
                .setSubject(id)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 토큰 파싱해서 subject 꺼내오기
    public String getSubjectFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
    }

    // 유효성 체크(만료 여부 등)
    public boolean validateToken(String token) {
        Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token); // 여기서 유효하지 않으면 예외 발생
        return true;
    }

    // 토큰에서 memberId 추출
    public Long getMemberIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(key)
                .parseClaimsJws(token)
                .getBody();
        return Long.valueOf(claims.get("memberId").toString());
    }
}
