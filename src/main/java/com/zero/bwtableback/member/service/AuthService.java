package com.zero.bwtableback.member.service;

import com.zero.bwtableback.member.dto.EmailLoginDto;
import com.zero.bwtableback.member.dto.SignUpDto;
import com.zero.bwtableback.member.dto.TokenDto;
import com.zero.bwtableback.member.entity.Member;

/**
 * 사용자 인증 및 권한 부여를 처리하는 서비스 인터페이스
 *
 * 로그인, 회원가입, 토큰 갱신 등 인증 관련 비즈니스 로직을 정의
 */
public interface AuthService {

    // 이메일 중복 확인
    boolean isEmailDuplicate(String email);

    // 닉네임 중복 확인
    boolean isNicknameDuplicate(String nickname);

    // 전화번호 중복 확인
    boolean isPhoneDuplicate(String phone);

    // 사업자등록번호 중복 확인
    boolean isBusinessNumberDuplicate(String businessNumber);

    // 새로운 사용자 회원가입
    Member signUp(SignUpDto form);

    // 사용자 로그인을 처리하고 인증 토큰을 반환
    TokenDto login(EmailLoginDto loginDto);

    // 리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급
    TokenDto refreshToken(String refreshToken);

    // 사용자 로그아웃 처리
    void logout(String email);
}