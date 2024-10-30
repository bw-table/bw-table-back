package com.zero.bwtableback.member.service;

import com.zero.bwtableback.member.dto.EmailLoginDto;
import com.zero.bwtableback.member.dto.SignUpDto;
import com.zero.bwtableback.member.dto.TokenDto;
import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.member.repository.MemberRepository;
import com.zero.bwtableback.security.jwt.TokenProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 사용자 인증 및 권한 부여를 처리하는 서비스 클래스
 *
 * 로그인, 회원가입, 토큰 갱신 등 인증 관련 비즈니스 로직 포함
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    public AuthServiceImpl(MemberRepository memberRepository, PasswordEncoder passwordEncoder, TokenProvider tokenProvider) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    /**
     * 새로운 사용자 회원가입
     */
    @Override
    public Member signUp(SignUpDto form) {
        // 이메일 유효성 검사 및 중복 체크
        validateEmail(form.getEmail());

        // 닉네임 유효성 검사 및 중복 체크
        validateNickname(form.getNickname());

        // 비밀번호 유효성 검사
        validatePassword(form.getPassword());

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(form.getPassword());
        form.setPassword(encodedPassword);

        // Member 객체 생성 및 저장
        Member member = Member.from(form);

        return memberRepository.save(member);
    }

    // 이메일 중복 체크 함수
    public boolean isEmailDuplicate(String email) {
        return memberRepository.existsByEmail(email);
    }

    // 닉네임 중복 체크 함수
    public boolean isNicknameDuplicate(String nickname) {
        return memberRepository.existsByNickname(nickname);
    }

    // 이메일 유효성 검사
    private void validateEmail(String email) {
        // FIXME Regex 수정 필요
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        if (email == null || email.length() < 3 || email.length() > 50 || !email.matches(emailRegex)) {
            throw new IllegalArgumentException("유효하지 않은 이메일 형식입니다.");
        }

        if (isEmailDuplicate(email)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
    }

    // 닉네임 유효성 검사
    private void validateNickname(String nickname) {
        if (nickname.length() < 2 || nickname.length() > 15 || !nickname.matches("^[a-zA-Z0-9가-힣]+$")) {
            throw new IllegalArgumentException("유효하지 않은 닉네임입니다.");
        }

        if (isNicknameDuplicate(nickname)) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }
    }

    // 비밀번호 유효성 검사
    private void validatePassword(String password) {
        if (password.length() < 8 || !password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,}$")) {
            throw new IllegalArgumentException("비밀번호는 최소 8자 이상이며 대문자, 소문자, 숫자 및 특수문자를 포함해야 합니다.");
        }
    }

    /**
     * 사용자 로그인을 처리하고 인증 토큰을 반환
     */
    public TokenDto login(EmailLoginDto loginDto) {
        Member member = memberRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new BadCredentialsException("이메일 또는 비밀번호가 유효하지 않습니다."));

        // 비밀번호 확인
        if (!passwordEncoder.matches(loginDto.getPassword(), member.getPassword())) {
            throw new BadCredentialsException("이메일 또는 비밀번호가 유효하지 않습니다.");
        }

        // 토큰 생성
        String accessToken = tokenProvider.createAccessToken(member.getEmail());
        String refreshToken = tokenProvider.createRefreshToken();

        // TODO 리프레시 토큰 저장 (레디스)

        // FIXME 리프레시 토큰 임시 저장 (Member DB)
        member.setRefreshToken(refreshToken);
        memberRepository.save(member);

        // TokenDto 생성 및 반환
        return new TokenDto(accessToken, refreshToken);
    }

    /**
     * 리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급
     */
    public TokenDto refreshToken(String refreshToken){

        return null;
    }

    /**
     * 사용자 로그아웃 처리
     */
    public void logout(String email){

    }
}
