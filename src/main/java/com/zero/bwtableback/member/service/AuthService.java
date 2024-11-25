package com.zero.bwtableback.member.service;

import com.zero.bwtableback.common.exception.CustomException;
import com.zero.bwtableback.common.exception.ErrorCode;
import com.zero.bwtableback.member.dto.*;
import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.member.entity.Role;
import com.zero.bwtableback.member.repository.MemberRepository;
import com.zero.bwtableback.restaurant.repository.RestaurantRepository;
import com.zero.bwtableback.security.jwt.TokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * 사용자 인증 및 권한 부여를 처리하는 서비스 클래스
 *
 * 로그인, 회원가입, 토큰 갱신 등 인증 관련 비즈니스 로직 포함
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final RestaurantRepository restaurantRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 이메일 중복 확인
     */
    public boolean isEmailDuplicate(DuplicateCheckReqDto request) {
        return memberRepository.existsByEmail(request.getEmail());
    }

    /**
     * 닉네임 중복 확인
     */
    public boolean isNicknameDuplicate(DuplicateCheckReqDto request) {
        return memberRepository.existsByNickname(request.getNickname());
    }

    /**
     * 전화번호 중복 확인
     */
    public boolean isPhoneDuplicate(DuplicateCheckReqDto request) {
        return memberRepository.existsByPhone(request.getPhone());
    }

    /**
     * 사업자등록번호 중복 확인 (사장님만)
     */
    public boolean isBusinessNumberDuplicate(DuplicateCheckReqDto request) {
        return memberRepository.existsByBusinessNumber(request.getBusinessNumber());
    }

    /**
     * 새로운 사용자 이메일 회원가입
     */
    public MemberDto signUp(SignUpReqDto form) {
        // 이메일 중복 체크
        if (memberRepository.existsByEmail(form.getEmail())) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        // 닉네임 중복 체크
        if (memberRepository.existsByNickname(form.getNickname())) {
            throw new CustomException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }

        // 전화번호 중복 체크
        if (memberRepository.existsByPhone(form.getPhone())) {
            throw new CustomException(ErrorCode.PHONE_ALREADY_EXISTS);
        }

        // 사업자등록번호 중복 체크 (사장님만)
        if (form.getBusinessNumber() != null &&
                memberRepository.existsByBusinessNumber(form.getBusinessNumber())) {
            throw new CustomException(ErrorCode.BUSINESS_NUMBER_ALREADY_EXISTS);
        }

        String encodedPassword = passwordEncoder.encode(form.getPassword());
        Member member = Member.from(form, encodedPassword);
        memberRepository.save(member);

        return MemberDto.from(member);
    }

    /**
     * 회원가입 및 자동 로그인
     */
    public LoginResDto signUpLogin(MemberDto memberDto, HttpServletRequest request, HttpServletResponse response) {

        String accessToken = tokenProvider.createAccessToken(memberDto.getEmail(), memberDto.getRole());
        String refreshToken = tokenProvider.createRefreshToken(memberDto.getId().toString());

        // HttpOnly 쿠키에 리프레시 토큰 저장
        saveRefreshTokenToCookie(refreshToken, response);

        // Redis에 리프레시 토큰 저장
        saveRefreshTokenToRedis(memberDto.getId(), refreshToken);

        return new LoginResDto(accessToken, memberDto, null);
    }

    /**
     * 로그인
     */
    public LoginResDto login(MemberDto memberDto, HttpServletRequest request, HttpServletResponse response) {

        String accessToken = tokenProvider.createAccessToken(memberDto.getEmail(), memberDto.getRole());
        String refreshToken = tokenProvider.createRefreshToken(memberDto.getId().toString());

        // 회원 상태 조회

        // HttpOnly 쿠키에 리프레시 토큰 저장
        saveRefreshTokenToCookie(refreshToken, response);

        // Redis에 리프레시 토큰 저장
        saveRefreshTokenToRedis(memberDto.getId(), refreshToken);

        // 레스토랑 ID 조회 (사장님일 경우)
        Long restaurantId = getRestaurantIdIfOwner(memberDto);


        return new LoginResDto(accessToken, memberDto, restaurantId);
    }

    // 회원 인증
    public MemberDto authenticateMember(EmailLoginReqDto loginReqDto) {
        Member member = memberRepository.findByEmail(loginReqDto.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(loginReqDto.getPassword(), member.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }

        return MemberDto.from(member);
    }

    // 리프레시 토큰으로 액세스 토큰 갱신
    public LoginResDto renewAccessTokenWithRefreshToken(String refreshToken) {
        String email = tokenProvider.getUsername(refreshToken);

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        validateRefreshToken(refreshToken, member.getId());

        String newAccessToken = tokenProvider.createAccessToken(email, member.getRole());

        MemberDto memberDto = MemberDto.from(member);

        return new LoginResDto(newAccessToken, memberDto, getRestaurantIdIfOwner(memberDto));
    }

    // 리프레시 토큰 검증
    public void validateRefreshToken(String refreshToken, Long memberId) {
        String key = "refresh_token:" + memberId;

        String storedRefreshToken = redisTemplate.opsForValue().get(key);

        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다.");
        }
    }

    // 쿠키에 리프레시 토큰 저장
    public void saveRefreshTokenToCookie(String refreshToken, HttpServletResponse response) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // HTTPS 환경에서만 전송
        cookie.setPath("/");
        cookie.setMaxAge(86400); // 1일 (86400초)
        response.addCookie(cookie);
    }

    // Redis에 리프레시 토큰 저장
    public void saveRefreshTokenToRedis(Long memberId, String refreshToken) {
        String key = "refresh_token:" + memberId;
        try {
            redisTemplate.opsForValue().set(key, refreshToken);
        } catch (RedisConnectionFailureException e) {
            System.err.println("Redis에 연결할 수 없습니다: " + e.getMessage());
        } catch (Exception e) {
            // 다른 예외 처리
            System.err.println("예기치 않은 오류 발생: " + e.getMessage());
        }
    }

    // 기존 유효한 Access Token이 존재하는 경우 처리
    public LoginResDto handleExistingToken(String existingToken) {
        String email = tokenProvider.getUsername(existingToken);

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        MemberDto memberDto = MemberDto.from(member);

        Long restaurantId = getRestaurantIdIfOwner(memberDto);

        return new LoginResDto(existingToken, memberDto, restaurantId);
    }

    // 사장님일 경우 레스토랑 ID 조회 메서드
    private Long getRestaurantIdIfOwner(MemberDto member) {
        if (member.getRole() == Role.OWNER) {
            return restaurantRepository.findRestaurantIdByMemberId(member.getId());
        }
        return null;
    }

    /**
     * 사용자 로그아웃 처리
     */
    public void logout(String email, HttpServletRequest request, HttpServletResponse response) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String key = "refresh_token:" + member.getId();
        redisTemplate.delete(key);

        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    /**
     * 사용자 회원탈퇴
     *
     * 로그아웃 처리 후
     */
}