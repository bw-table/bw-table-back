package com.zero.bwtableback.member.service;

import com.zero.bwtableback.common.exception.CustomException;
import com.zero.bwtableback.common.exception.ErrorCode;
import com.zero.bwtableback.member.dto.*;
import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.member.repository.MemberRepository;
import com.zero.bwtableback.security.jwt.TokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 사용자 인증 및 권한 부여를 처리하는 서비스 클래스
 *
 * 로그인, 회원가입, 토큰 갱신 등 인증 관련 비즈니스 로직 포함
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    private final RedisTemplate<String, String> redisTemplate;

    private static final int REFRESH_TOKEN_TTL = 86400; // FIXME 환경변수 처리

    /**
     * 이메일 중복 확인
     */
    public boolean isEmailDuplicate(String email) {
        return memberRepository.existsByEmail(email);
    }

    /**
     * 닉네임 중복 확인
     */
    public boolean isNicknameDuplicate(String nickname) {
        return memberRepository.existsByNickname(nickname);
    }

    /**
     * 전화번호 중복 확인
     */
    public boolean isPhoneDuplicate(String phone) {
        return memberRepository.existsByPhone(phone);
    }

    /**
     * 사업자등록번호 중복 확인 (사장님만)
     */
    public boolean isBusinessNumberDuplicate(String businessNumber) {
        return memberRepository.existsByBusinessNumber(businessNumber);
    }

    /**
     * 새로운 사용자 이메일 회원가입
     */
    public SignUpResDto signUp(SignUpReqDto form) {
        // 이메일 중복 체크
        if (isEmailDuplicate(form.getEmail())) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        // 닉네임 중복 체크
        if (isNicknameDuplicate(form.getNickname())) {
            throw new CustomException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }
        // 전화번호 중복 체크
        if (isPhoneDuplicate(form.getPhone())) {
            throw new CustomException(ErrorCode.PHONE_ALREADY_EXISTS);
        }
        // 사업자등록번호 유효성 검사(사장님 회원가입 시)
        if ("OWNER".equals(form.getRole())) {

            // 사업자등록번호 중복 체크
            if (isBusinessNumberDuplicate(form.getBusinessNumber())) {
                throw new CustomException(ErrorCode.MISSING_BUSINESS_NUMBER);
            }
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(form.getPassword());

        Member member = Member.from(form, encodedPassword);

        Member savedMember = memberRepository.save(member);

        return SignUpResDto.from(savedMember);
    }

    /**
     * 사용자 로그인을 처리하고 인증 토큰을 반환
     */
    public LoginResDto login(EmailLoginReqDto loginDto, HttpServletRequest request, HttpServletResponse response) {
        String existingToken = tokenProvider.extractToken(request);

        // 기존 토큰이 존재하는 경우 유효한 경우
        if (existingToken != null && tokenProvider.validateToken(existingToken)) {
            return handleExistingToken(existingToken);
        }


        // 기존 토큰이 존재하지만 유효하지 않은 경우
        if (existingToken != null && !tokenProvider.validateToken(existingToken)) {
            // 리프레시 토큰 확인하고 accessToken 반환
            String refreshToken = getRefreshTokenFromRequest(request); // 요청에서 리프레시 토큰 추출

            if (refreshToken != null && tokenProvider.validateToken(refreshToken)) {
                // 리프레시 토큰이 유효한 경우 새로운 액세스 토큰 생성
                String email = tokenProvider.getUsername(refreshToken); // 이메일 추출
                return handleValidRefreshToken(email, response);
            } else {
                throw new CustomException(ErrorCode.INVALID_TOKEN);
            }
        }

        // 새로운 로그인 처리
        return handleNewLogin(loginDto, response);
    }

    // 기존 유효한 AccessToken이 존재하는 경우
    private LoginResDto handleExistingToken(String existingToken) {
        String email = tokenProvider.getUsername(existingToken);
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        MemberDto memberDto = MemberDto.from(member);
        return new LoginResDto(existingToken, memberDto);
    }

    // 새로운 로그인 처리 - 첫 로그인 시, 토큰 없을 시, 토큰이 만료된 경우
    private LoginResDto handleNewLogin(EmailLoginReqDto loginDto, HttpServletResponse response) {
        Member member = memberRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 비밀번호 유효성 검사
        if (!passwordEncoder.matches(loginDto.getPassword(), member.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }

        // 토큰 생성
        String accessToken = tokenProvider.createAccessToken(member.getEmail());
        String refreshToken = tokenProvider.createRefreshToken();

        // HttpOnly 쿠키에 리프레시 토큰 저장
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // HTTPS 환경에서만 전송
        cookie.setPath("/");
        cookie.setMaxAge(86400); // 1일 (86400초)

        response.addCookie(cookie);

        // 리프레시 토큰을 레디스에 저장
        String key = "refresh_token:" + member.getId();
        redisTemplate.opsForValue().set(key, refreshToken);

        MemberDto memberDto = MemberDto.from(member);
        return new LoginResDto(accessToken, memberDto);
    }

    // 리프레시 토큰을 사용하여 새로운 액세스 토큰 발급
    private LoginResDto handleValidRefreshToken(String email, HttpServletResponse response) {
        // 사용자 정보를 데이터베이스에서 가져오기
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String newAccessToken = tokenProvider.createAccessToken(member.getEmail());

        MemberDto memberDto = MemberDto.from(member);
        return new LoginResDto(newAccessToken, memberDto);
    }

    // 요청에서 리프레시 토큰 추출
    private String getRefreshTokenFromRequest(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * TODO 사용자 로그아웃 처리
     */
    public void logout(String email) {
//        Member member = memberRepository.findById()
//                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    }
}