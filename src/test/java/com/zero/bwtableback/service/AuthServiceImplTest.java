package com.zero.bwtableback.service;

import com.zero.bwtableback.member.dto.SignUpDto;
import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.member.entity.Role;
import com.zero.bwtableback.member.repository.MemberRepository;
import com.zero.bwtableback.member.service.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.xml.validation.Validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * FIXME 테스트 코드 설명 (삭제 예정)
 *
 * @Mock mock 객체를 생성할 때 사용한다.
 * @MockBean 스프링 ApplicationContext에 mock 빈을 주입할 때 사용
 * @InjectMocks mock 객체를 주입받을 대상에 사용하며, 주입받을 필드에 mock 객체가 자동으로 주입
 * @assertThrows 메서드는 두 개의 인자를 받음
 * 예외 클래스: 발생할 것으로 예상되는 예외의 클래스
 * Executable: 예외가 발생해야 하는 코드 블록입니다. 일반적으로 람다 표현식으로 작성
 */

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private SignUpDto form;

    @BeforeEach
    void setUp() {
        form = new SignUpDto();
        form.setRole(Role.OWNER); // 사업자등록번호는 조건이 필요하므로 사장님으로 등록
        form.setEmail("test@example.com");
        form.setNickname("길동");
        form.setPassword("Test123@");
        form.setPhone("01012341234");
        form.setBusinessNumber("0122233333");
    }

    @Test
    @DisplayName("회원가입 성공")
    void testSignupOwnerSuccess() {
        // given
        when(memberRepository.existsByEmail(form.getEmail())).thenReturn(false);
        when(memberRepository.existsByNickname(form.getNickname())).thenReturn(false);
        when(passwordEncoder.encode(form.getPassword())).thenReturn("encodedPassword");

        Member member = new Member();
        member.setEmail(form.getEmail());
        member.setNickname(form.getNickname());
        member.setPassword("encodedPassword");

        when(memberRepository.save(any(Member.class))).thenReturn(member);

        // when
        Member result = authService.signUp(form);

        // then
        assertEquals("test@example.com", result.getEmail());
        assertEquals("길동", result.getNickname());
        assertEquals("encodedPassword", result.getPassword());

        // save 메서드가 한 번 호출되었는지 검증
        verify(memberRepository, times(1)).save(any(Member.class));
    }

    // 중복 검사
    @Test
    @DisplayName("이메일 중복 검사")
    void testSignupEmailDuplicate() {
        // given
        when(memberRepository.existsByEmail(form.getEmail())).thenReturn(true);

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.signUp(form);
        });

        assertEquals("이미 사용 중인 이메일입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("닉네임 중복 검사")
    void testSignupNicknameDuplicate() {
        // given
        when(memberRepository.existsByNickname(form.getNickname())).thenReturn(true);

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.signUp(form);
        });

        assertEquals("이미 사용 중인 닉네임입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("전화번호 중복 검사")
    void testSignupPhoneDuplicate() {
        // given
        when(memberRepository.existsByPhone(form.getPhone())).thenReturn(true);

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.signUp(form);
        });

        assertEquals("이미 사용 중인 전화번호입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("사업자등록번호 중복 검사")
    void testSignupBusinessDuplicate() {
        // given
        when(memberRepository.existsByBusinessNumber(form.getBusinessNumber())).thenReturn(true);

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.signUp(form);
        });

        assertEquals("이미 사용 중인 사업자등록번호입니다.", exception.getMessage());
    }

//    //FIXME 유효성 검사 컨트롤러에서 검사 예정
//    @Test
//    @DisplayName("이메일 유효성 검사")
//    void signup_invalidEmail() {
//        // given
//        form.setEmail("notemail");
//
//        // when & then
//        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
//            authService.signUp(form);
//        });
//        System.out.println(exception.getMessage());
//        assertEquals("유효한 이메일 주소를 입력하세요.", exception.getMessage());
//    }
//
//    @Test
//    @DisplayName("닉네임 유효성 검사")
//    void signup_invalidNickname() {
//        // given
//        form.setNickname("!!invalid!!");
//
//        // when & then
//        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
//            authService.signUp(form);
//        });
//        System.out.println(exception.getMessage());
//        assertEquals("유효하지 않은 닉네임입니다.", exception.getMessage());
//    }
//
//    @Test
//    @DisplayName("비밀번호 유효성 검사")
//    void signup_invalidPassword() {
//        // given
//        form.setPassword("weakpass");
//
//        // when & then
//        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
//            authService.signUp(form);
//        });
//
//        assertEquals("비밀번호는 최소 8자 이상이며 대문자, 소문자, 숫자 및 특수문자를 포함해야 합니다.", exception.getMessage());
//    }
}