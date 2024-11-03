package com.zero.bwtableback.service;

import com.zero.bwtableback.common.exception.CustomException;
import com.zero.bwtableback.common.exception.ErrorCode;
import com.zero.bwtableback.member.dto.EmailLoginReqDto;
import com.zero.bwtableback.member.dto.SignUpReqDto;
import com.zero.bwtableback.member.dto.SignUpResDto;
import com.zero.bwtableback.member.dto.TokenDto;
import com.zero.bwtableback.member.entity.LoginType;
import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.member.entity.Role;
import com.zero.bwtableback.member.repository.MemberRepository;
import com.zero.bwtableback.member.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

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
class AuthServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
//    private TokenProvider tokenProvider;

    @InjectMocks
    private AuthService authService;

    private SignUpReqDto signUpForm;

    //FIXME DTO에서 SET 사용에 대한 의논 필요 (코드리뷰X)
//    @BeforeEach
//    void setUp() {
//        signUpForm = new SignUpReqDto();
//        signUpForm.setEmail("test@example.com");
//        signUpForm.setPassword("securePassword123");
//        signUpForm.setName("홍길동");
//        signUpForm.setNickname("길동");
//        signUpForm.setPhone("01012345678");
//        signUpForm.setLoginType("EMAIL");
//        signUpForm.setRole("OWNER");
//    }

    @Test
    @DisplayName("회원가입 성공")
    void testSignUpSuccess() {
        // given
        when(memberRepository.existsByEmail(signUpForm.getEmail())).thenReturn(false);
        when(memberRepository.existsByNickname(signUpForm.getNickname())).thenReturn(false);
        when(memberRepository.existsByPhone(signUpForm.getPhone())).thenReturn(false);
        when(passwordEncoder.encode(signUpForm.getPassword())).thenReturn("encodedPassword");

        Member savedMember = Member.builder()
                .id(1L)
                .email(signUpForm.getEmail())
                .name(signUpForm.getName())
                .nickname(signUpForm.getNickname())
                .password("encodedPassword")
                .phone(signUpForm.getPhone())
                .loginType(LoginType.EMAIL)
                .role(Role.GUEST)
                .build();

        when(memberRepository.save(any(Member.class))).thenReturn(savedMember);

        // when
        SignUpResDto result = authService.signUp(signUpForm);

        // then
        assertEquals("test@example.com", result.getEmail());
        assertEquals("홍길동", result.getName());
        assertEquals("길동", result.getNickname());

        verify(memberRepository, times(1)).save(any(Member.class));
    }

    @Test
    @DisplayName("이메일 중복으로 회원가입 실패")
    void testSignUpEmailDuplicate() {
        // given
        when(memberRepository.existsByEmail(signUpForm.getEmail())).thenReturn(true);

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            authService.signUp(signUpForm);
        });

        assertEquals(ErrorCode.EMAIL_ALREADY_EXISTS, exception.getErrorCode());
    }

    @Test
    @DisplayName("로그인 성공")
    void testLoginSuccess() {
        // given
        EmailLoginReqDto loginDto = new EmailLoginReqDto();
//        loginDto.setEmail("test@example.com");
//        loginDto.setPassword("securePassword123");

        Member member = Member.builder()
                .id(1L)
                .email(loginDto.getEmail())
                .password(passwordEncoder.encode(loginDto.getPassword()))
                .build();

        when(memberRepository.findByEmail(loginDto.getEmail())).thenReturn(java.util.Optional.of(member));
        when(passwordEncoder.matches(loginDto.getPassword(), member.getPassword())).thenReturn(true);

        // FIXME 로직 변경 필요
//        String accessToken = "accessToken";
//        String refreshToken = "refreshToken";


//        when(tokenProvider.createAccessToken(member.getEmail())).thenReturn(accessToken);
//        when(tokenProvider.createRefreshToken()).thenReturn(refreshToken);

        // when
        TokenDto tokenDto = authService.login(loginDto);

        // then
//        assertEquals(accessToken, tokenDto.getAccessToken());
//        assertEquals(refreshToken, tokenDto.getRefreshToken());

        verify(memberRepository, times(1)).findByEmail(loginDto.getEmail());
    }

    @Test
    @DisplayName("로그인 실패 - 잘못된 이메일")
    void testLoginInvalidEmail() {
        // given
        EmailLoginReqDto loginDto = new EmailLoginReqDto();
//        loginDto.setEmail("test@example.com");

        when(memberRepository.findByEmail(loginDto.getEmail())).thenReturn(java.util.Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            authService.login(loginDto);
        });

        assertEquals(ErrorCode.INVALID_CREDENTIALS, exception.getErrorCode());
    }

    @Test
    @DisplayName("로그인 실패 - 잘못된 비밀번호")
    void testLoginInvalidPassword() {
        // given
        EmailLoginReqDto loginDto = new EmailLoginReqDto();

//        loginDto.setEmail("test@example.com");

        Member member = Member.builder()
                .id(1L)
                .email(loginDto.getEmail())
                .password(passwordEncoder.encode("securePassword123"))
                .build();

        when(memberRepository.findByEmail(loginDto.getEmail())).thenReturn(java.util.Optional.of(member));
        when(passwordEncoder.matches(loginDto.getPassword(), member.getPassword())).thenReturn(false);

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            authService.login(loginDto);
        });

        assertEquals(ErrorCode.INVALID_CREDENTIALS, exception.getErrorCode());
    }
}