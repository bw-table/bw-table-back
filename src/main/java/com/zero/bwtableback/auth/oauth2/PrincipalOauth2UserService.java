package com.zero.bwtableback.auth.oauth2;

import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.member.entity.Role;
import com.zero.bwtableback.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

/**
 * OAuth2 인증을 처리하는 사용자 정의 서비스
 *
 * - 소셜 로그인을 통해 인증된 사용자 정보를 로드
 * - 해당 사용자의 정보를 데이터베이스에 저장/업데이트
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PrincipalOauth2UserService extends DefaultOAuth2UserService {

//    private final MemberRepository memberRepository;
////    private final BCryptPasswordEncoder encoder;
//
//    @Override
//    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
//        OAuth2User oAuth2User = super.loadUser(userRequest);
//        log.info("getAttributes : {}", oAuth2User.getAttributes());
//
//        OAuth2UserInfo oAuth2UserInfo = null;
//
//        String provider = userRequest.getClientRegistration().getRegistrationId();
//
//        if (provider.equals("kakao")) {
//            log.info("카카오 로그인 요청");
//            oAuth2UserInfo = new KakaoUserInfo((Map<String, Object>) oAuth2User.getAttributes());
//        }
//
//        String providerId = oAuth2UserInfo.getProviderId();
//        String email = oAuth2UserInfo.getEmail();
//        String loginId = provider + "_" + providerId;
//        String nickname = oAuth2UserInfo.getName();
//
//
//        Optional<Member> optionalUser = memberRepository.findByLoginId(loginId);
//        Member member = null;
//
//        if (optionalUser.isEmpty()) {
//            member = member.builder()
////                    .loginId(loginId)
//                    .nickname(nickname)
//                    .provider(provider)
//                    .providerId(providerId)
//                    .role(Role.GUEST)
//                    .build();
//            memberRepository.save(member);
//        } else {
//            member = optionalUser.get();
//        }
//
//        return new PrincipalDetails(member, oAuth2User.getAttributes());
//    }
}
