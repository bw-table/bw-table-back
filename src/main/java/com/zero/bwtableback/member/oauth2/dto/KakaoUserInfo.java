package com.zero.bwtableback.member.oauth2.dto;

import lombok.AllArgsConstructor;
import lombok.Setter;

import java.util.Map;

/**
 * 카카오 사용자 정보
 *
 * 비즈앱 등록을 하지 않은 경우 카카오에서 개인정보를 제공하지 않으므로 임시 값으로 대체.
 */
@Setter
@AllArgsConstructor
public class KakaoUserInfo {

    private Map<String, Object> attributes;

    public String getProviderId() {
        return attributes.get("id").toString();
    }

    public String getProvider() {
        return "kakao";
    }

    public String getEmail() {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        return kakaoAccount.get("email") != null ? (String) kakaoAccount.get("email") : "kakao@mail.com";
    }

    public String getName() {
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
        return properties.get("name") != null ? (String) properties.get("name") : "라이언";
    }

    public String getNickName() {
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
        return properties.get("nickname") != null ? (String) properties.get("nickname") : "까까오";
    }

    public String getPhone() {
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
        return properties.get("phone") != null ? (String) properties.get("phone") : "01098765432";
    }

    public String getProfileImage() {
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
        return properties.get("profile_image") != null ? (String) properties.get("profile_image") : null;
    }
}
