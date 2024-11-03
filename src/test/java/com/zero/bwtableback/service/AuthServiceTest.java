package com.zero.bwtableback.service;

import com.zero.bwtableback.member.dto.SignUpReqDto;
import com.zero.bwtableback.member.dto.SignUpResDto;
import com.zero.bwtableback.member.entity.LoginType;
import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.member.entity.Role;
import com.zero.bwtableback.member.repository.MemberRepository;
import com.zero.bwtableback.member.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

// FIXME 이슈번호-#19번에서 테스트 코드 변경 (코드리뷰X)
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private SignUpReqDto form;

    @BeforeEach
    void setUp() {
        form = new SignUpReqDto(
                LoginType.EMAIL,
                Role.GUEST,
                "test@example.com",
                "홍길동",
                "Test123@",
                "길동",
                "01012345678"
        );
    }

    @Test
    void testSignupMemberSuccess() {
        when(memberRepository.existsByEmail(form.getEmail())).thenReturn(false);
        when(memberRepository.existsByNickname(form.getNickname())).thenReturn(false);
        when(passwordEncoder.encode(form.getPassword())).thenReturn("encodedPassword");

        Member savedMember = Member.builder()
                .id(1L)
                .email(form.getEmail())
                .name(form.getName())
                .nickname(form.getNickname())
                .password("encodedPassword")
                .phone(form.getPhone())
                .loginType(form.getLoginType())
                .role(form.getRole())
                .build();

        when(memberRepository.save(any(Member.class))).thenReturn(savedMember);

        SignUpResDto result = authService.signUp(form);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("홍길동", result.getName());
        assertEquals("길동", result.getNickname());
        assertEquals("01012345678", result.getPhone());
        assertEquals(LoginType.EMAIL, result.getLoginType());
        assertEquals(Role.GUEST, result.getRole());

        // password는 SignUpResponseDto에 포함되지 않아야 함
        assertThrows(NoSuchMethodException.class, () -> SignUpResDto.class.getDeclaredMethod("getPassword"));

        // save 메서드가 한 번 호출되었는지 검증
        verify(memberRepository, times(1)).save(any(Member.class));
    }

    @Test
    void testSignupMemberEmailDuplicate() {
        when(memberRepository.existsByEmail(form.getEmail())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.signUp(form);
        });

        assertEquals("이미 사용 중인 이메일입니다.", exception.getMessage());
    }

    @Test
    void testSignupMemberNicknameDuplicate() {
        when(memberRepository.existsByNickname(form.getNickname())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.signUp(form);
        });

        assertEquals("이미 사용 중인 닉네임입니다.", exception.getMessage());
    }

    @Test
    void signup_invalidEmail() {
        form.setEmail("hel");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.signUp(form);
        });

        assertEquals("유효하지 않은 이메일 형식입니다.", exception.getMessage());
    }

    @Test
    void signup_invalidNickname() {
        form.setNickname("!!invalid!!");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.signUp(form);
        });

        assertEquals("유효하지 않은 닉네임입니다.", exception.getMessage());
    }

    @Test
    void signup_invalidPassword() {
        form.setPassword("weakpass");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.signUp(form);
        });

        assertEquals("비밀번호는 최소 8자 이상이며 대문자, 소문자, 숫자 및 특수문자를 포함해야 합니다.", exception.getMessage());
    }
}