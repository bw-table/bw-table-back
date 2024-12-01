package com.zero.bwtableback.member.servcie;

import com.zero.bwtableback.BwTableBackApplication;
import com.zero.bwtableback.common.exception.CustomException;
import com.zero.bwtableback.common.exception.ErrorCode;
import com.zero.bwtableback.member.dto.*;
import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.member.entity.Role;
import com.zero.bwtableback.member.repository.MemberRepository;
import com.zero.bwtableback.member.service.AuthService;
import com.zero.bwtableback.security.jwt.TokenProvider;
import io.jsonwebtoken.lang.Assert;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ContextConfiguration(classes = BwTableBackApplication.class)
class AuthServiceTest {

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @InjectMocks
    private AuthService authService;

    private SignUpReqDto signUpForm;
    private MemberDto memberDto;

    @BeforeEach
    void setUp() {
        signUpForm = SignUpReqDto.builder()
                .email("test@example.com")
                .password("testPassword123@")
                .name("홍길동")
                .nickname("길동")
                .phone("01012345678")
                .loginType("EMAIL")
                .role("GUEST")
                .build();

        memberDto = MemberDto.builder()
                .id(1L)
                .email("test@example.com")
                .role(Role.GUEST)
                .build();
    }

    @Test
    @DisplayName("회원 정보 중복 검사 - 이메일, 닉네임, 전화번호, 사업자등록번호")
    void testDuplicateCheck() {
        // Given
        DuplicateCheckReqDto emailRequest = DuplicateCheckReqDto.builder()
                .email("test@example.com")
                .build();

        DuplicateCheckReqDto nicknameRequest = DuplicateCheckReqDto.builder()
                .nickname("테스트")
                .build();

        DuplicateCheckReqDto phoneRequest = DuplicateCheckReqDto.builder()
                .phone("01012341234")
                .build();

        DuplicateCheckReqDto businessNumberRequest = DuplicateCheckReqDto.builder()
                .businessNumber("123-45-67890")
                .build();

        when(memberRepository.existsByEmail(emailRequest.getEmail())).thenReturn(true);
        when(memberRepository.existsByNickname(nicknameRequest.getNickname())).thenReturn(false);
        when(memberRepository.existsByPhone(phoneRequest.getPhone())).thenReturn(true);
        when(memberRepository.existsByBusinessNumber(businessNumberRequest.getBusinessNumber())).thenReturn(false);

        // When
        boolean isEmailDuplicate = authService.isEmailDuplicate(emailRequest);
        boolean isNicknameDuplicate = authService.isNicknameDuplicate(nicknameRequest);
        boolean isPhoneDuplicate = authService.isPhoneDuplicate(phoneRequest);
        boolean isBusinessNumberDuplicate = authService.isBusinessNumberDuplicate(businessNumberRequest);

        // Then
        assertTrue(isEmailDuplicate);
        assertFalse(isNicknameDuplicate);
        assertTrue(isPhoneDuplicate);
        assertFalse(isBusinessNumberDuplicate);

        verify(memberRepository).existsByEmail(emailRequest.getEmail());
        verify(memberRepository).existsByNickname(nicknameRequest.getNickname());
        verify(memberRepository).existsByPhone(phoneRequest.getPhone());
        verify(memberRepository).existsByBusinessNumber(businessNumberRequest.getBusinessNumber());
    }

    @Test
    @DisplayName("이메일 중복 확인 - 새로운 이메일로 회원가입 성공")
    void testSignUpNewEmail() {
        // given
        when(memberRepository.existsByEmail(signUpForm.getEmail())).thenReturn(false);

        // when
        MemberDto result = authService.signUp(signUpForm);

        // then
        assertNotNull(result);
        assertEquals(signUpForm.getEmail(), result.getEmail());

        verify(memberRepository).save(any(Member.class));
    }

    @Test
    @DisplayName("이메일 중복 확인 - 중복 이메일로 회원가입 시 예외 발생")
    void testSignUpEmailDuplicate() {
        // given
        when(memberRepository.existsByEmail(signUpForm.getEmail())).thenReturn(true);

        // when
        // then
        CustomException exception = assertThrows(CustomException.class, () -> {
            authService.signUp(signUpForm);
        });

        assertEquals(ErrorCode.EMAIL_ALREADY_EXISTS, exception.getErrorCode());

        verify(memberRepository, never()).save(any());
    }

    @Test
    @DisplayName("회원가입 성공")
    public void testSignUp() {
        // Given

        // When
        MemberDto savedMember = authService.signUp(signUpForm);

        // Then
        assertNotNull(savedMember);
        Assert.notNull(savedMember.getRole());
        Assert.notNull(savedMember.getEmail());
        Assert.notNull(savedMember.getNickname());
        Assert.notNull(savedMember.getPhone());

        verify(memberRepository).save(any(Member.class));
    }

    @Test
    @DisplayName("토큰 발급")
    public void testGetToken() {
        // Given
        when(tokenProvider.createAccessToken(memberDto.getEmail(), memberDto.getRole())).thenReturn("mockAccessToken");
        when(tokenProvider.createRefreshToken(anyString())).thenReturn("mockRefreshToken");

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        // When
        LoginResDto loginResDto = authService.login(memberDto, request, response);

        // Then
        assertNotNull(loginResDto);
        assertNotNull(loginResDto.getAccessToken());

        System.out.println("Access Token: " + loginResDto.getAccessToken());
    }

    @Test
    @DisplayName("인증 후 로그인 성공")
    void testLoginSuccess() {
        // given
        String email = "test@example.com";
        String password = "testPassword123@";
        String encodedPassword = passwordEncoder.encode(password);

        Member testMember = Member.builder()
                .id(1L)
                .email(email)
                .password(encodedPassword)
                .name("TestUser")
                .build();

        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(testMember));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);

        EmailLoginReqDto loginDto = EmailLoginReqDto.builder()
                .email("test@example.com")
                .password("testPassword123@")
                .build();

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        // when
        MemberDto authenticatedMember = authService.authenticateMember(loginDto);
        LoginResDto loginResDto = authService.login(authenticatedMember, request, response);

        // then
        assertNotNull(loginResDto);
        System.out.println(loginResDto.getMember().getName());
    }


    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 이메일")
    void testLoginNotExistsEmail() {
        // given
        String email = "test@example.com";
        String password = "testPassword123@";
        String encodedPassword = passwordEncoder.encode(password);

        Member testMember = Member.builder()
                .id(1L)
                .email(email)
                .password(encodedPassword)
                .name("TestUser")
                .build();

        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(testMember));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);

        EmailLoginReqDto loginDto = EmailLoginReqDto.builder()
                .email("invalid@example.com")
                .password("testPassword123@")
                .build();

        // when
        CustomException exception = assertThrows(CustomException.class, () -> {
            authService.authenticateMember(loginDto);
        });

        // then
        assertEquals(ErrorCode.INVALID_CREDENTIALS, exception.getErrorCode());
    }

    @Test
    @DisplayName("로그인 실패 - 잘못된 비밀번호")
    void testLoginInvalidPassword() {
        // given
        String email = "test@example.com";
        String password = "testPassword123@";
        String encodedPassword = passwordEncoder.encode(password);

        Member testMember = Member.builder()
                .id(1L)
                .email(email)
                .password(encodedPassword)
                .name("TestUser")
                .build();


        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(testMember));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(false);

        EmailLoginReqDto loginDto = EmailLoginReqDto.builder()
                .email("test@example.com")
                .password("wrongNumber123")
                .build();

        // when
        CustomException exception = assertThrows(CustomException.class, () -> {
            authService.authenticateMember(loginDto);
        });

        // then
        assertEquals(ErrorCode.INVALID_CREDENTIALS, exception.getErrorCode());
    }

    // FIXME Redis opsForValue 메서드 null 반환 문제
//    @Test
//    @DisplayName("리프레시 토큰으로 액세스 토큰 갱신 성공")
//    void testRenewAccessTokenWithRefreshToken() {
//        // given
//        String refreshToken = "valid_refresh_token";
//        String email = "test@example.com";
//        Long memberId = 1L;
//        String key = "refresh_token:" + memberId;
//        Member member = Member.builder()
//                .id(memberId)
//                .email(email)
//                .role(Role.GUEST)
//                .build();
//        String newAccessToken = "new_access_token";
//
//        when(tokenProvider.getUsername(refreshToken)).thenReturn(email);
//        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
//        when(redisTemplate.opsForValue().get(key)).thenReturn(refreshToken);
//        when(tokenProvider.createAccessToken(email, member.getRole())).thenReturn(newAccessToken);
//
//        // when
//        LoginResDto result = authService.renewAccessTokenWithRefreshToken(refreshToken);
//
//        System.out.println(result.getAccessToken());
//
//        // then
//        assertNotNull(result);
//        assertEquals(email, result.getMember().getEmail());
//        assertEquals(newAccessToken, result.getAccessToken());
//
//        verify(memberRepository).findByEmail(email);
//        verify(tokenProvider).getUsername(refreshToken);
//        verify(redisTemplate.opsForValue()).get("refresh_token:" + memberId);
//        verify(tokenProvider).createAccessToken(email, member.getRole());
//    }
//
//    @Test
//    @DisplayName("유효하지 않은 리프레시 토큰으로 액세스 토큰 갱신 실패")
//    void testRenewAccessTokenWithInvalidRefreshToken() {
//        // given
//        String refreshToken = "invalid_refresh_token";
//        String email = "test@example.com";
//        Long memberId = 1L;
//        Member member = Member.builder()
//                .id(memberId)
//                .email(email)
//                .role(Role.GUEST)
//                .build();
//
//        when(tokenProvider.getUsername(refreshToken)).thenReturn(email);
//        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
//        when(redisTemplate.opsForValue().get("refresh_token:" + memberId)).thenReturn("different_token");
//
//        // when & then
//        assertThrows(ResponseStatusException.class, () -> {
//            authService.renewAccessTokenWithRefreshToken(refreshToken);
//        });
//    }

    @Test
    @DisplayName("로그아웃 성공")
    void logoutTest() {
        // given
        String email = "test@example.com";
        Long memberId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .email(email)
                .build();

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));

        // when
        authService.logout(email, response);

        // then
        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class); // HttpServletResponse에 추가된 쿠키 캡처
        verify(response).addCookie(cookieCaptor.capture());

        Cookie capturedCookie = cookieCaptor.getValue();
        assertEquals("refreshToken", capturedCookie.getName());
        assertNull(capturedCookie.getValue());
        assertTrue(capturedCookie.isHttpOnly());
        assertTrue(capturedCookie.getSecure());
        assertEquals("/", capturedCookie.getPath());
        assertEquals(0, capturedCookie.getMaxAge());
    }
}