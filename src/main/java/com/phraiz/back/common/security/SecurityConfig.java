package com.phraiz.back.common.security;

import com.phraiz.back.common.security.jwt.JwtAuthenticationFilter;
import com.phraiz.back.common.security.jwt.JwtUtil;
import com.phraiz.back.common.security.oauth.CustomOAuth2SuccessHandler;
import com.phraiz.back.member.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final RedisTemplate<String, String> redisTemplate;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomOAuth2SuccessHandler customOAuth2SuccessHandler;


    // BCryptPasswordEncoder 등록
    // 비밀번호를 안전하게 암호화하기 위해 사용
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 특정 HTTP 요청에 대한 웹 기반 보안 구성
    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf->csrf.disable())
//                .cors(cors -> cors.configurationSource(exchange -> {
//                    CorsConfiguration config = new CorsConfiguration();
//                    config.setAllowedOrigins(List.of("", "http://localhost:3000", "http://localhost:8080", "https://ssu-phraiz-fe.vercel.app/"));
//                    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
//                    config.setAllowedHeaders(List.of("*"));
//                    config.setExposedHeaders(List.of("Authorization"));
//                    config.setAllowCredentials(true);
//                    return config;
//                }))
                .sessionManagement(session->session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorizeRequests ->authorizeRequests
                        // TODO 인증 없이 접근 허용한 부분?
                        .requestMatchers("/api/members/refresh","/api/members/signUp","/api/members/login","/api/members/findUserId","/api/members/emails/**","/api/cite/**").permitAll()
                        .requestMatchers("/api/members/logout").authenticated()
                        .anyRequest().authenticated()
                )
                .oauth2Login((oauth2Login) ->{
                    oauth2Login
                          //  .loginPage("/login")
                            .userInfoEndpoint(userInfoEndpointConfig ->
                            userInfoEndpointConfig.userService(customOAuth2UserService))
                            .successHandler(customOAuth2SuccessHandler);
                })
                .addFilterBefore(new JwtAuthenticationFilter(jwtUtil, userDetailsService,redisTemplate), UsernamePasswordAuthenticationFilter.class)
                .build();

    }

    // 로그인 시 사용자의 id/pw를 DB에서 불러와서 검증하는 로직을 Spring Security에 등록
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(bCryptPasswordEncoder());
        return daoAuthenticationProvider;
    }

    // 로그인 시 사용할 AuthenticationManager를 빈으로 등록해서 외부에서 사용할 수 있도록
    @Bean
    public AuthenticationManager authenticationManagerBean(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // 스프링 시큐리티 기능 비활성화
//    @Bean
//    public WebSecurityCustomizer configure(){
//        return (web)->web.ignoring()
//                .requestMatchers(PathRequest.toH2Console())
//                .requestMatchers("/static/**");
//    }


}
