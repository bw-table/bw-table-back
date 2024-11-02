package com.zero.bwtableback.auth.oauth2;

import com.zero.bwtableback.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
public class PrincipalDetails implements UserDetails, OAuth2User {
    private Member member; // Member 객체
    private Map<String, Object> attributes; // OAuth2 사용자 정보

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null; // 필요에 따라 권한을 설정
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return member.getEmail(); // 이메일을 사용자 이름으로 사용
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

    // OAuth2User 메서드 구현
    @Override
    public String getName() {
        return (String) attributes.get("name"); // 사용자 이름 (예: 닉네임)
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes; // OAuth2 사용자 정보 반환
    }

    public Member getMember() {
        return member; // Member 객체 반환 (추가)
    }
}

