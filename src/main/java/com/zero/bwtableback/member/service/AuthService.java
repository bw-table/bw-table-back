package com.zero.bwtableback.member.service;

import com.zero.bwtableback.common.exception.CustomException;
import com.zero.bwtableback.common.exception.ErrorCode;
import com.zero.bwtableback.member.dto.EmailLoginReqDto;
import com.zero.bwtableback.member.dto.SignUpReqDto;
import com.zero.bwtableback.member.dto.SignUpResDto;
import com.zero.bwtableback.member.dto.TokenDto;
import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.member.entity.Role;
import com.zero.bwtableback.member.repository.MemberRepository;
import com.zero.bwtableback.security.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
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

    /**
     * 새로운 사용자 회원가입
     */
    public SignUpResDto signUp(SignUpReqDto form) {
        // 이메일 유효성 검사 및 중복 체크
        validateEmail(form.getEmail());

        // 닉네임 유효성 검사 및 중복 체크
        validateNickname(form.getNickname());

        // 비밀번호 유효성 검사
        validatePassword(form.getPassword());

        // 사업자등록번호 유효성 검사 및 하이픈 제거(사장님 회원가입 시)
        if (form.getRole() == Role.OWNER) {
            validateBusinessNumber(form);
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(form.getPassword());

        Member member = Member.from(form, encodedPassword);

        Member savedMember = memberRepository.save(member);

        return SignUpResDto.from(savedMember);
    }

    // FIXME 아래 모든 유효성 검사 @Valid로 처리 예정 (코드리뷰X)
    private void validateEmail(String email) {
        // FIXME Regex 수정 필요
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        if (email == null || email.length() < 3 || email.length() > 50 || !email.matches(emailRegex)) {
            throw new CustomException(ErrorCode.INVALID_EMAIL_FORMAT);
        }

        if (isEmailDuplicate(email)) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
    }

    // 닉네임 유효성 검사
    private void validateNickname(String nickname) {
        if (nickname.length() < 2 || nickname.length() > 15 || !nickname.matches("^[a-zA-Z0-9가-힣]+$")) {
            throw new CustomException(ErrorCode.INVALID_NICKNAME_FORMAT);
        }

        if (isNicknameDuplicate(nickname)) {
            throw new CustomException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }
    }

    // 비밀번호 유효성 검사
    private void validatePassword(String password) {
        if (password.length() < 8 || !password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,}$")) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD_FORMAT);
        }
    }

    // 이메일 중복 체크 함수
    private boolean isEmailDuplicate(String email) {
        return memberRepository.existsByEmail(email);
    }

    // 닉네임 중복 체크 함수
    private boolean isNicknameDuplicate(String nickname) {
        return memberRepository.existsByNickname(nickname);
    }

    // 사장님 회원가입 시 사업자등록번호 체크 함수
    private void validateBusinessNumber(SignUpReqDto form) {
        if (form.getBusinessNumber() == null || form.getBusinessNumber().trim().isEmpty()) {
            throw new CustomException(ErrorCode.MISSING_BUSINESS_NUMBER);
        }

        // 사업자등록번호 형식 예시: 123-01-11111
        if (form.getBusinessNumber().trim().length() != 12) {
            throw new CustomException(ErrorCode.INVALID_BUSINESS_NUMBER_FORMAT);
        }
    }

    private String cleanBusinessNumber(String businessNumber) {
        return businessNumber.trim().replaceAll("-", "");
    }

    /**
     * 사용자 로그인을 처리하고 인증 토큰을 반환
     */
    public TokenDto login(EmailLoginReqDto loginDto) {
        Member member = memberRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CREDENTIALS));

        // 비밀번호 확인
        if (!passwordEncoder.matches(loginDto.getPassword(), member.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }

        // 토큰 생성
        String accessToken = tokenProvider.createAccessToken(member.getEmail());
        String refreshToken = tokenProvider.createRefreshToken();

        // TODO 리프레시 토큰 저장 (레디스)

        // FIXME 리프레시 토큰 임시 저장 (Member DB) 저장소 변경 후 setter Member의 @Setter 삭제
        member.setRefreshToken(refreshToken);
        memberRepository.save(member);

        // TokenDto 생성 및 반환
        return new TokenDto(accessToken, refreshToken);
    }

    /**
     * 리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급
     */
    public TokenDto refreshToken(String refreshToken) {

        return null;
    }

    /**
     * 사용자 로그아웃 처리
     */
    public void logout(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    }
}