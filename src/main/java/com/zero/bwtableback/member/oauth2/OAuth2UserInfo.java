package com.zero.bwtableback.member.oauth2;

/**
 * OAuth2 사용자 정보 추상화 인터페이스
 */
public interface OAuth2UserInfo {
    String getProviderId();

    // OAuth2 제공자의 이름을 반환
    String getProvider();
    String getEmail();
    String getName();
}
