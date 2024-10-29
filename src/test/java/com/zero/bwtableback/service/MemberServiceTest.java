package com.zero.bwtableback.service;

import com.zero.bwtableback.member.dto.SignupForm;
import com.zero.bwtableback.member.entity.LoginType;
import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.member.entity.Role;
import com.zero.bwtableback.member.repository.MemberRepository;
import com.zero.bwtableback.member.service.MemberServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * FIXME
 * @Mock
 * mock 객체를 생성할 때 사용한다.
 *
 * @MockBean
 * 스프링 ApplicationContext에 mock 빈을 주입할 때 사용
 *
 * @InjectMocks
 * mock 객체를 주입받을 대상에 사용하며, 주입받을 필드에 mock 객체가 자동으로 주입
 *
 * @assertThrows 메서드는 두 개의 인자를 받음
 * 예외 클래스: 발생할 것으로 예상되는 예외의 클래스
 * Executable: 예외가 발생해야 하는 코드 블록입니다. 일반적으로 람다 표현식으로 작성
 */

@ExtendWith(MockitoExtension.class)
class MemberServiceImplTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private MemberServiceImpl memberService;

    private SignupForm form;

    @BeforeEach
    void setUp() {
        form = new SignupForm();
        form.setEmail("test@example.com");
        form.setNickname("길동");
        form.setPassword("Test123@");
    }

    @Test
    void testSignupMemberSuccess() {
        when(memberRepository.existsByEmail(form.getEmail())).thenReturn(false);
        when(memberRepository.existsByNickname(form.getNickname())).thenReturn(false);
        when(passwordEncoder.encode(form.getPassword())).thenReturn("encodedPassword");

        Member member = new Member();
        member.setEmail(form.getEmail());
        member.setNickname(form.getNickname());
        member.setPassword("encodedPassword");

        when(memberRepository.save(any(Member.class))).thenReturn(member);

        Member result = memberService.signupMember(form);

        assertEquals("test@example.com", result.getEmail());
        assertEquals("길동", result.getNickname());
        assertEquals("encodedPassword", result.getPassword());
        verify(memberRepository, times(1)).save(any(Member.class));
    }

    @Test
    void testSignupMemberEmailDuplicate() {
        when(memberRepository.existsByEmail(form.getEmail())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            memberService.signupMember(form);
        });

        assertEquals("이미 사용 중인 이메일입니다.", exception.getMessage());
    }

    @Test
    void testSignupMemberNicknameDuplicate() {
        when(memberRepository.existsByNickname(form.getNickname())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            memberService.signupMember(form);
        });

        assertEquals("이미 사용 중인 닉네임입니다.", exception.getMessage());
    }

    @Test
    void signup_invalidEmail() {
        form.setEmail("hel");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            memberService.signupMember(form);
        });

        assertEquals("유효하지 않은 이메일 형식입니다.", exception.getMessage());
    }

    @Test
    void signup_invalidNickname() {
        form.setNickname("!!invalid!!");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            memberService.signupMember(form);
        });

        assertEquals("유효하지 않은 닉네임입니다.", exception.getMessage());
    }

    @Test
    void signup_invalidPassword() {
        form.setPassword("weakpass");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            memberService.signupMember(form);
        });

        assertEquals("비밀번호는 최소 8자 이상이며 대문자, 소문자, 숫자 및 특수문자를 포함해야 합니다.", exception.getMessage());
    }
}