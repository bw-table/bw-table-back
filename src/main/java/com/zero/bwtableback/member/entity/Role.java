package com.zero.bwtableback.member.entity;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Collections;

public enum Role implements GrantedAuthority {
    GUEST,
    OWNER;

    @Override
    public String getAuthority() {
        return name(); // 역할 이름을 권한으로 사용
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(this); // 단일 권한으로 설정
    }
}
