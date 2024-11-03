package com.zero.bwtableback.security;

import lombok.AllArgsConstructor;
import lombok.Setter;
import org.apache.catalina.User;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 사용자 정보를 담고 있는 객체로, 주로 인증 및 권한 부여에 사용.
 *
 * FIXME 코드 리뷰 (X) : 토큰에서 검증과 함께 구현 예정
 */
@Setter
@AllArgsConstructor
public class UserPrincipal {
//    private String email;
//    private String password;
//    private Collection<? extends GrantedAuthority> authorities; // 권한
//    private Map<String, Object> attributes; // 추가적인 사용자 정보 (예: OAuth2 사용자 정보)
//
//    public UserPrincipal(String email, String password, Collection<? extends GrantedAuthority> authorities) {
//        this.email = email;
//        this.password = password;
//        this.authorities = authorities;
//        this.attributes = new HashMap<>();
//    }
//
//    public static UserPrincipal create(User user) {
//        return new UserPrincipal(
//                user.getUsername(),
//                user.getPassword(),
//                null // 권한을 설정하는 로직 필요
//        );
//    }
//
//    public static UserPrincipal create(User user, Map<String, Object> attributes) {
//        UserPrincipal userPrincipal = create(user);
//        userPrincipal.setAttributes(attributes);
//        return userPrincipal;
//    }
}
