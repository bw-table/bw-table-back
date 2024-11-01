package com.zero.bwtableback.auth.oauth2;

import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.member.entity.Role;
import com.zero.bwtableback.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
//        // 소셜 플랫폼에서 제공 받은 사용자 정보를 객체로 변환
//        if (provider.equals("kakao")) {
//            log.info("카카오 로그인 요청");
//            oAuth2UserInfo = new KakaoUserInfo((Map<String, Object>) oAuth2User.getAttributes());
//        }
//
//        String providerId = oAuth2UserInfo.getProviderId();
//        String email = oAuth2UserInfo.getEmail();
//        /**
//         * loginId - 서로 다른 소셜 플랫폼의 사용자를 구분
//         *
//         * 예시
//         * - 카카오 사용자: "kakao_12345"
//         * - 구글 사용자: "google_67890"
//         */
//        String socialId = provider + "_" + providerId;
//        String nickname = oAuth2UserInfo.getName();
//
//
//        Optional<Member> optionalUser = memberRepository.findBySocialId(socialId);
//        Member member = null;
//
//        // 사용자가 없으면 새로운 멤버 객체 생성
//        if (optionalUser.isEmpty()) {
//            member = member.builder()
//                    .socialId(socialId)
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
//        // 인증 객체 반환 (Spring Security의 인증 컨텍스트에서 사용)
//        return new PrincipalDetails(member, oAuth2User.getAttributes());
//    }
}
