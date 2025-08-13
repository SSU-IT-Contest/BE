package com.phraiz.back.member.service;

import com.phraiz.back.common.exception.custom.BusinessLogicException;
import com.phraiz.back.common.util.RedisUtil;
import com.phraiz.back.member.domain.Member;
import com.phraiz.back.member.dto.response.LoginResponseDTO;
import com.phraiz.back.member.exception.MemberErrorCode;
import com.phraiz.back.member.repository.MemberRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
// 인증코드 생성하고 이메일 보내는 서비스
public class EmailService {
    private static final String AUTH_KEY_PREFIX = "auth:";
    private static final String VERIFIED_KEY_PREFIX = "verified:";
    private final JavaMailSender emailSender;
    private final RedisUtil redisUtil;
    private final MemberRepository memberRepository;

    // 임의의 6자리 양수 반환
    public String makeRandNum(){
        Random rand = new Random();
        StringBuilder randNum= new StringBuilder();
        for(int i=0;i<6;i++){
            randNum.append(Integer.toString(rand.nextInt(10)));
        }
        return randNum.toString();
    }

    // 랜덤 인증번호 생성-> 메일 발송
    public void joinEmail(String email) throws MessagingException {
        String authNum = makeRandNum();
        String title = "회원 가입 인증 이메일 입니다."; // 이메일 제목
        String content =
                "<p>Phraiz</p>" + 	//html 형식으로 작성
                        "<br><br>" +
                        "인증 번호는 <b>" + authNum + "</b>입니다." +
                        "<br>" ; //이메일 내용 삽입

        sendMail(email, title, content,authNum);

    }
    // 이메일 전송
    public void sendMail(String to, String subject, String text, String authNum) throws MessagingException {

        MimeMessage mimeMessage = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "utf-8");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text, true);
        try {
            emailSender.send(mimeMessage);
        } catch (MailException e) {
            System.err.println("이메일 전송 실패: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("이메일 전송 불가능",e);
        }
        if (authNum != null) {
            // Redis에 인증번호 저장-검증을 위함
            // 이메일이 key, 인증번호가 value
            String redisKey = AUTH_KEY_PREFIX + to; // "auth:user@example.com"
            redisUtil.setDataExpire(redisKey, authNum, 60*5L);

        }


    }
    // 사용자가 입력한 인증 번호와 실제 인증 번호 비교
    public boolean checkAuthNum(String email, String authNum) {
        String redisKey = AUTH_KEY_PREFIX + email;
        String storedAuthNum = redisUtil.getData(redisKey);
        // 저장된 인증번호가 없음 (만료되었거나 발급되지 않음)
        if (storedAuthNum == null) {
            //log.warn("인증번호 검증 실패: 저장된 인증번호 없음, email={}", email);
            throw new BusinessLogicException(MemberErrorCode.VERIFICATION_CODE_EXPIRED);
        }
        // 입력된 인증번호와 저장된 인증번호 비교
        if (storedAuthNum.equals(authNum)) { // 인증 성공
            // 1. 사용된 인증번호 삭제
            redisUtil.deleteData(redisKey);
            // 2. 인증 완료 표시
            String verifiedKey = VERIFIED_KEY_PREFIX + email;
            //redisUtil.setDataExpire(verifiedKey, "true", 60*15L); // 인증 완료 후 제한 시간
            redisUtil.setData(verifiedKey, "true"); // 인증 완료 후 제한 시간x
            return true;
        }else {
            // 인증번호 검사 실패
            throw new BusinessLogicException(MemberErrorCode.WRONG_VERIFICATION_CODE);
        }

    }
    // 이메일로 사용자 찾기 존재->true
    public boolean getMemberByEmail(String email) {
        // 등록된 이메일인지 확인
        return memberRepository.existsByEmail(email);
    }

}
