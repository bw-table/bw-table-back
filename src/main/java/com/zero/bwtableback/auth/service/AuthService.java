package com.zero.bwtableback.auth.service;

import com.zero.bwtableback.member.dto.EmailLoginDto;
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
 * TokenProvider를 사용하여 JWT 토큰을 생성, 관리
 */
@Service
public class AuthService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    public AuthService(MemberRepository memberRepository, PasswordEncoder passwordEncoder, TokenProvider tokenProvider) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

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

    // 토큰 갱신
    // 로그아웃 등
}
