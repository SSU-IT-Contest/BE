package com.phraiz.back.common.security.jwt;

import com.phraiz.back.common.exception.custom.InternalServerException;
import com.phraiz.back.common.exception.GlobalErrorCode;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// HTTP 요청에서 JWT 토큰을 꺼내고, 유효한 경우 인증된 사용자로 등록
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil; // 검증, 추출
    private final UserDetailsService userDetailsService; // id로 사용자 정보 불러오는 시큐리티 인터페이스
    private final RedisTemplate<String, String> redisTemplate;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService, RedisTemplate<String, String> redisTemplate) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.redisTemplate = redisTemplate;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token=getJwtToken(request);
        System.out.println("JwtAuthenticationFilter 작동중, 요청 URI: " + request.getRequestURI());
        try{
            if(token!=null && jwtUtil.validateToken(token)){ // 토큰 존재&유효
                // 블랙리스트 토큰 검사 추가
                try {
                    String isLogout = redisTemplate.opsForValue().get(token);
                    if ("logout".equals(isLogout)) {
                        // 이미 로그아웃된 토큰 → 인증 거부
                        throw new InternalServerException(GlobalErrorCode.AUTH_TOKEN_LOGGED_OUT);
                    }
                } catch (DataAccessException e) {
                    throw new InternalServerException(GlobalErrorCode.AUTH_REDIS_ERROR, e);
                }
//                String isLogout = redisTemplate.opsForValue().get(token);
//                if ("logout".equals(isLogout)) {
//                    // 이미 로그아웃된 토큰 → 인증 거부
//                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                    response.getWriter().write("This token is logged out.");
//                    return;
//                }
                String id=jwtUtil.getSubjectFromToken(token); // 사용자 id 추출
                UserDetails userDetails;
                try {
                    userDetails = userDetailsService.loadUserByUsername(id);
                } catch (UsernameNotFoundException e) {
                    throw new InternalServerException(GlobalErrorCode.AUTH_USER_NOT_FOUND, e);
                } catch (DataAccessException e) {
                    throw new InternalServerException(GlobalErrorCode.DATABASE_ERROR, e);
                } catch (Exception e) {
                    throw new InternalServerException(GlobalErrorCode.AUTH_INTERNAL, e);
                }
                //UserDetails userDetails = userDetailsService.loadUserByUsername(id);
                try {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()); // 시큐리티에서 사용하는 인증 객체 생성
                    authentication.setDetails(new WebAuthenticationDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication); // 현재 요청에 대해 "인증된 사용자" 로 등록
                } catch (IllegalStateException e) {
                    throw new InternalServerException(GlobalErrorCode.AUTH_SECURITY_CONTEXT_ERROR, e);
                }
//                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()); // 시큐리티에서 사용하는 인증 객체 생성
//                authentication.setDetails(new WebAuthenticationDetails(request));
//                SecurityContextHolder.getContext().setAuthentication(authentication); // 현재 요청에 대해 "인증된 사용자" 로 등록
        }

        filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Access token has expired.");
            return;
        } catch (JwtException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid access token.");
            return;
        }catch (InternalServerException e) {
            // 위에서 래핑한 예외는 그대로 전파
            throw e;
        }

    }

    // jwt 추출
    public String getJwtToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return token;
    }

}
