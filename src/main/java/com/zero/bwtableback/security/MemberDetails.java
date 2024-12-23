package com.zero.bwtableback.security;

import com.zero.bwtableback.member.entity.Member;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * Spring Security와 통합된 사용자 인증 및 권한 부여 시스템
 */
@Builder
@RequiredArgsConstructor
public class MemberDetails implements UserDetails {
    // Member 객체 반환
    private final Member member;

    public Member getMember() {
        return member;
    }

    public Long getMemberId() {
        return member.getId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 사용자의 역할을 권한으로 변환하여 반환
        return member.getRole().getAuthorities();
    }

    @Override
    public String getUsername() {
        return member.getEmail(); // 이메일을 사용자 이름으로 사용
    }

    @Override
    public String getPassword() {
        return member.getPassword(); // 비밀번호 반환
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // 계정 만료 여부
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // 계정 잠금 여부
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 자격 증명 만료 여부
    }

    @Override
    public boolean isEnabled() {
        return true; // 계정 활성화 여부
    }
}
