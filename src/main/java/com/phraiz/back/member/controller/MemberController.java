package com.phraiz.back.member.controller;

import com.phraiz.back.common.exception.custom.BusinessLogicException;
import com.phraiz.back.common.security.jwt.JwtUtil;
import com.phraiz.back.common.security.user.CustomUserDetails;
import com.phraiz.back.member.domain.Member;
import com.phraiz.back.member.dto.request.EmailCheckDTO;
import com.phraiz.back.member.dto.request.EmailRequestDTO;
import com.phraiz.back.member.dto.request.LoginRequestDTO;
import com.phraiz.back.member.dto.request.SignUpRequestDTO;
import com.phraiz.back.member.dto.response.LoginResponseDTO;
import com.phraiz.back.member.dto.response.SignUpResponseDTO;
import com.phraiz.back.member.exception.MemberErrorCode;
import com.phraiz.back.member.service.EmailService;
import com.phraiz.back.member.service.MemberService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;

    // accessToken 재발급
    // AccessToken은 만료기간이 짧기 때문에, 매번 로그인하는 대신 RefreshToken으로 연장
    // 토큰 갱신 시 Access Token과 Refresh Token을 모두 새로 발급
    @PostMapping("/reissue")
    public ResponseEntity<LoginResponseDTO> refreshToken(@CookieValue("refreshToken") String refreshToken,
                                                         HttpServletResponse response) {
        // 서비스에서 새로운 토큰들 생성
       LoginResponseDTO responseDTO=memberService.reissueToken(refreshToken);

//       // 새로운 refresh 토큰을 redis에서 가져오기
//        String newRefreshToken=redisTemplate.opsForValue().get("RT:"+responseDTO.getId());
//        // 새로운 refresh 토큰을 쿠키에 저장
//        Cookie refreshTokenCookie = memberService.addCookie(newRefreshToken);

        //response.addCookie(refreshTokenCookie);

       return ResponseEntity.ok(responseDTO);

    }

    /* 1. 회원가입 */
    // 1-1. 회원 가입(+아이디,이메일 중복 체크)
    @PostMapping("/signUp")
    public ResponseEntity<SignUpResponseDTO> signUp(@RequestBody SignUpRequestDTO signUpRequestDTO) {
        //memberService.checkEmailVerified(signUpRequestDTO.getEmail());
        SignUpResponseDTO signUpResponseDTO = memberService.signUp(signUpRequestDTO);
        return ResponseEntity.ok(signUpResponseDTO);
    }

    // 1-2. 이메일 인증 번호 전송&인증
    @PostMapping("/emails/mailSend")
    public ResponseEntity<Map<String, Object>> sendEmail(@RequestBody EmailRequestDTO emailRequestDTO) {
        if (emailService.getMemberByEmail(emailRequestDTO.getEmail())){
            throw new BusinessLogicException(MemberErrorCode.DUPLICATE_EMAIL);
        }
        Map<String, Object> response = new HashMap<>();
        System.out.println("이메일 인증 요청이 들어옴");
        System.out.println("이메일 인증이메일: "+emailRequestDTO.getEmail());
        try {
            emailService.joinEmail(emailRequestDTO.getEmail());
            response.put("success", true);
            response.put("message", "인증번호가 발송되었습니다.");
            return ResponseEntity.ok(response);
        } catch (MessagingException e) {
            response.put("success", false);
            response.put("message", "이메일 발송에 실패했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    // 1-2. 인증코드 확인
    @PostMapping("/emails/mailAuthCheck")
    public ResponseEntity<Map<String, Object>> authCheck(@RequestBody @Valid EmailCheckDTO emailCheckDTO) {
        boolean check=emailService.checkAuthNum(emailCheckDTO.getEmail(),emailCheckDTO.getAuthNum());

        if (!check) {
            throw new BusinessLogicException(MemberErrorCode.WRONG_VERIFICATION_CODE);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "이메일 인증 성공");
        return ResponseEntity.ok(response);
    }

    /* 2. 로그인 */
    // 2-1. 로그인
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO loginRequestDTO, HttpServletResponse response) {
        Member member = memberService.login(loginRequestDTO);

        // 토큰 생성
        String accessToken = jwtUtil.generateToken(member.getId());
        String refreshToken = jwtUtil.generateRefreshToken(member.getId());

        // refresh token redis에 저장
        redisTemplate.opsForValue().set(
                "RT:"+member.getId(),
                refreshToken,
                jwtUtil.getRefreshTokenExpTime(),TimeUnit.MILLISECONDS
        );

        // refresh token을 httpOnly 쿠키에 저장
        Cookie refreshTokenCookie = memberService.addCookie(refreshToken);

        response.addCookie(refreshTokenCookie);

        // 응답 DTO에서는 refreshToken 제거
        LoginResponseDTO loginResponseDTO = new LoginResponseDTO(accessToken,member.getMemberId(),member.getId(),member.getEmail(),member.getRole());
        return ResponseEntity.ok(loginResponseDTO);
    }
    // 2-2. 로그아웃
    // access 토큰 받아서 처리
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(@RequestHeader(value = "Authorization",  required = false) String token,
                                                      @CookieValue(value = "refreshToken", required = false) String refreshToken,
                                         @AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                      HttpServletResponse httpResponse) {
            // 토큰 유효성 검증
            if (!jwtUtil.validateToken(token.replace("Bearer ", ""))) {
                throw new BusinessLogicException(MemberErrorCode.INVALID_ACCESS_TOKEN);
            }

            // bearer 제거(access token)
            String jwt = token.replace("Bearer ", "");
            // jwt 만료 시간 계산
            long expire = jwtUtil.getAccessTokenExpTime();
            // redis에 저장(키: 토큰, value: "logout", ttl: 만료시간)
            // accessToken 블랙리스트 등록
            redisTemplate.opsForValue().set(jwt, "logout", expire, TimeUnit.MILLISECONDS);
            // refreshToken redis에서 삭제
            redisTemplate.delete("RT:" + customUserDetails.getUsername());

            // 쿠키 삭제
            Cookie deleteCookie=new Cookie("refreshToken", null);
            deleteCookie.setMaxAge(0);
            deleteCookie.setPath("/");
            deleteCookie.setHttpOnly(true);
            httpResponse.addCookie(deleteCookie);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "logout success");

            return ResponseEntity.ok(response);

    }

    // 2-3. 이메일 입력 시 이메일로 아이디 전송
    @PostMapping("/findId")
    public ResponseEntity<Map<String, Object>> findId(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        Map<String, Object> response = new HashMap<>();
        memberService.findId(email);
        response.put("success", true);
        response.put("message", "입력하신 이메일로 아이디를 전송했습니다.");
        return ResponseEntity.ok(response);
    }


}
