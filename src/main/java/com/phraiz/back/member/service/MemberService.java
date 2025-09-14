package com.phraiz.back.member.service;

import com.phraiz.back.common.enums.Plan;
import com.phraiz.back.common.exception.custom.BusinessLogicException;
import com.phraiz.back.common.exception.custom.InvalidRefreshTokenException;
import com.phraiz.back.common.exception.custom.RefreshTokenExpiredException;
import com.phraiz.back.common.security.jwt.JwtUtil;
import com.phraiz.back.common.util.RedisUtil;
import com.phraiz.back.member.domain.Member;
import com.phraiz.back.member.dto.request.LoginRequestDTO;
import com.phraiz.back.member.dto.request.SignUpRequestDTO;
import com.phraiz.back.member.dto.response.LoginResponseDTO;
import com.phraiz.back.member.dto.response.SignUpResponseDTO;
import com.phraiz.back.member.enums.LoginType;
import com.phraiz.back.member.exception.MemberErrorCode;
import com.phraiz.back.member.repository.MemberRepository;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.Cookie;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.TimeUnit;


@Slf4j
@Service
public class MemberService {
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private EmailService emailService;

    // token 재발급-RTR 방식으로
    public LoginResponseDTO reissueToken(String refreshToken) {
        // refresh token 유효성 검사
//        if (!jwtUtil.validateToken(refreshToken)) {
//            throw new BusinessLogicException(MemberErrorCode.INVALID_REFRESH_TOKEN);
//        }
        try {
            jwtUtil.validateToken(refreshToken);
        } catch (JwtException e) {
            throw new InvalidRefreshTokenException("유효하지 않은 리프레시 토큰입니다.");
        }

        String id=jwtUtil.getSubjectFromToken(refreshToken); // 사용자의 고유 아이디
        Member member = memberRepository.findById(id).orElseThrow(()-> new BusinessLogicException(MemberErrorCode.USER_NOT_FOUND));

        // redis 에 저장된 refresh 토큰과 일치확인
        String storedRefreshToken=redisTemplate.opsForValue().get("RT:"+id);

        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            throw new InvalidRefreshTokenException("저장된 refresh token과 일치하지 않습니다.");
        }
        // 새로운 access token 발급
        String newAccessToken = jwtUtil.generateAccessToken(id, member.getMemberId());
        // 새로운 refresh token 발급
        //String newRefreshToken = jwtUtil.generateAccessToken(id);

        // Redis 업데이트: 기존 삭제 후 새로운 것 저장
//        redisTemplate.delete("RT:"+id);
//        redisTemplate.opsForValue().set(
//                "RT:"+id,
//                newRefreshToken,jwtUtil.getRefreshTokenExpTime(), TimeUnit.MILLISECONDS
//        );

        return new LoginResponseDTO(newAccessToken,
                member.getMemberId(),
                member.getId(),
                member.getEmail(),
                member.getRole(),
                member.getPlanId());
    }

    // 쿠키에 저장
    public Cookie addCookie(String refreshToken){
        // refresh token을 httpOnly 쿠키에 저장
        // 헤더에는 access만 남기고 refresh는 HttpOnly 쿠키에만 저장하는 구조가 더 안전
        // Access는 클라이언트가 직접 들고 있다가, 요청할 때마다 헤더에 넣어서 보내기
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        // 새로운 refresh 토큰을 쿠키에 저장
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true); // https 에서만 전송
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge((int) (jwtUtil.getRefreshTokenExpTime() / 1000)); // 초 단위로 변환
        refreshTokenCookie.setAttribute("SameSite", "None"); // Spring에서 직접 지원 안하면 response 헤더로 수동 추가 필요
        return refreshTokenCookie;
    }

    // 회원 요금제 반영
    @Transactional
    public void updatePlan(Long memberId, Long planId){
        // 식별자로 사용자 조회
        Member member=memberRepository.findByMemberId(memberId).orElseThrow(()->new BusinessLogicException(MemberErrorCode.USER_NOT_FOUND));
        member.setPlanId(planId);
    }


    /* 1. 회원가입 */
    // 1-1. 회원가입
    public SignUpResponseDTO signUp(SignUpRequestDTO signUpRequestDTO) {
        // 중복 검사 + 등록된 이메일인지 확인
        if (memberRepository.existsById(signUpRequestDTO.getId())) {
            throw new BusinessLogicException(MemberErrorCode.USERID_EXISTS);
        }
        if (memberRepository.existsByEmail(signUpRequestDTO.getEmail())) {
            throw new BusinessLogicException(MemberErrorCode.DUPLICATE_EMAIL);
        }
        checkEmailVerified(signUpRequestDTO.getEmail());
        // 비밀번호 암호화
        String encodedPwd=bCryptPasswordEncoder.encode(signUpRequestDTO.getPwd());

        Member member=Member.builder()
                .id(signUpRequestDTO.getId())
                .pwd(encodedPwd)
                .email(signUpRequestDTO.getEmail())
                .loginType(signUpRequestDTO.getLoginType())
                .planId(signUpRequestDTO.getPlanId())
                .build();
        memberRepository.save(member);

        // 회원가입 성공 후 Redis에서 인증 완료 상태 삭제
        redisUtil.deleteData("verified:" + signUpRequestDTO.getEmail());

        return new SignUpResponseDTO(member.getMemberId(),"회원가입 성공");
    }

    // 1-2. 아이디중복 확인
    public boolean checkId(String id){
        if (memberRepository.existsById(id)) {
            throw new BusinessLogicException(MemberErrorCode.USERID_EXISTS);
        }else {
            return true;
        }
    }

    // 1-3. 이메일 검증 여부 확인
    public void checkEmailVerified(String email) {
        String verified=redisUtil.getData("verified:"+email);
        if (!"true".equals(verified)) {
            throw new BusinessLogicException(MemberErrorCode.EMAIL_NOT_VERIFIED);
        }
    }

    /* 2. 로그인 */
    // 2-1. 로그인
    public Member login(LoginRequestDTO loginRequestDTO) {
        // 아이디로 사용자 조회
        Member member=memberRepository.findById(loginRequestDTO.getId()).orElseThrow(()->new BusinessLogicException(MemberErrorCode.USER_NOT_FOUND));

       if (!bCryptPasswordEncoder.matches(loginRequestDTO.getPwd(),member.getPwd())){
           throw new BusinessLogicException(MemberErrorCode.PASSWORD_MISMATCH);
       }
       return member;
    }

    // 2-3. 아이디 찾기
    // 등록된 이메일 입력 시 이메일로 아이디 전송
    public void findId(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessLogicException(MemberErrorCode.EMAIL_NOT_REGISTERED));
        LoginType loginType=member.getLoginType();
        if (!loginType.equals(LoginType.LOCAL)){
            throw  new BusinessLogicException(MemberErrorCode.SOCIAL_USER_NO_ID);
        }
            String id=member.getId();
            String title = "회원 아이디입니다.";
            String content =
                    "<p>Phraiz</p>" + 	//html 형식으로 작성
                            "<br><br>" +
                            "아이디는 <b>" + id + "</b>입니다." +
                            "<br>" ; //이메일 내용 삽입

        try {
            emailService.sendMail(email,title,content,null);
        } catch (MessagingException e) {
            throw new RuntimeException("이메일 전송 실패",e);
        }
    }

    // 2-4. 비밀번호 찾기
    /*
    * 임시 재설정 토큰(예: UUID or JWT) 하나만 발급 (액세스 토큰 아님)

    이 토큰은: DB 또는 Redis에 저장, 30분 등의 짧은 유효기간 설정

    프론트엔드가 이 토큰을 가지고 새 비밀번호를 입력하게 함, 백엔드는 이 토큰을 검증하고 새 비밀번호를 저장

    * */
    // 등록된 이메일 입력 시 이메일로 비밀번호 재설정 링크 전송
    public void findPwd(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessLogicException(MemberErrorCode.EMAIL_NOT_REGISTERED));
        LoginType loginType=member.getLoginType();
        if (!loginType.equals(LoginType.LOCAL)){
            throw  new BusinessLogicException(MemberErrorCode.SOCIAL_USER_NO_ID);
        }

        // 임시 토큰 생성
        // 1. 재설정용 토큰 생성 (UUID)
        String token = UUID.randomUUID().toString();

        // 2. Redis에 저장 (30분 만료)
        redisTemplate.opsForValue().set("PWD_RESET:" + token, member.getId(), 30, TimeUnit.MINUTES);

        // 3. 이메일 링크 생성
        // String resetLink = "https://ssu-phraiz-fe.vercel.app/reset-pw?token=" + token;
        // TODO 링크 변경
        String resetLink = "http://localhost:3000?token=" + token;

        String title = "비밀번호 재설정 안내";
        String content =
                "<p>안녕하세요,</p>" + 	//html 형식으로 작성
                        "<br><br>" +
                        "아래 링크를 클릭하여 비밀번호를 재설정해주세요. " +
                        "<br>" +
                        resetLink +
                        "<br>" +
                        "이 링크는 30분간 유효합니다. " +
                        "<br><br>" ; //이메일 내용 삽입


        try {
            emailService.sendMail(email,title,content,null);
        } catch (MessagingException e) {
            throw new RuntimeException("이메일 전송 실패",e);
        }

    }

    // 비밀번호 재설정 유효성 검사
    public boolean verifyResetToken(String token) {
        // 1. Redis에서 토큰 조회
        String resetId = redisTemplate.opsForValue().get("PWD_RESET:" + token);
        if (resetId == null) {
            throw new BusinessLogicException(MemberErrorCode.INVALID_PASSWORD_RESET_TOKEN);
        }

        // 2. 사용자 조회
        memberRepository.findById(resetId)
                .orElseThrow(() -> new BusinessLogicException(MemberErrorCode.USER_NOT_FOUND));

        return true;
    }

    // 비밀번호 재설정
    public void resetPwd(String token, String newPwd){
        // 1. Redis에서 토큰 조회
        String resetId = redisTemplate.opsForValue().get("PWD_RESET:" + token);
        if (resetId == null) {
            throw new BusinessLogicException(MemberErrorCode.INVALID_PASSWORD_RESET_TOKEN);
        }

        // 2. 사용자 조회
        Member member = memberRepository.findById(resetId)
                .orElseThrow(() -> new BusinessLogicException(MemberErrorCode.USER_NOT_FOUND));

        // 3. 비밀번호 암호화 및 저장
        // 비밀번호 암호화
        member.setPwd(bCryptPasswordEncoder.encode(newPwd));
        memberRepository.save(member);

        // 4. 토큰 삭제 (1회성)
        redisTemplate.delete("PWD_RESET:" + token);
    }


    // 3. 회원정보 가져오기
    public LoginResponseDTO getMember(String id, String accessToken) {
        Member member=memberRepository.findById(id)
                .orElseThrow(()->new UsernameNotFoundException("존재하지 않는 사용자입니다."));

        return new LoginResponseDTO(accessToken,
                member.getMemberId(),
                member.getId(),
                member.getEmail(),
                member.getRole(),
                member.getPlanId());
    }



}
