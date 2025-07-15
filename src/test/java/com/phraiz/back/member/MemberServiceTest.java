package com.phraiz.back.member;

import com.phraiz.back.member.domain.Member;
import com.phraiz.back.member.dto.request.SignUpRequestDTO;
import com.phraiz.back.member.enums.LoginType;
import com.phraiz.back.member.repository.MemberRepository;
import com.phraiz.back.member.service.MemberService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    public void signUp() {
//        // given
//        SignUpRequestDTO dto = new SignUpRequestDTO();
//        dto.setId("hee");
//        dto.setPwd("1234");
//        dto.setEmail("hee@gmail.com");
//        dto.setLoginType(LoginType.LOCAL);
//        // when
//        memberService.signUp(dto);
//        // then
//        Member saved = memberRepository.findById("hee").orElse(null);
//        assert saved != null;
//        assert saved.getEmail().equals("hee@gmail.com");
    }
}
