package com.zero.bwtableback.member.oauth2.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zero.bwtableback.common.exception.CustomException;
import com.zero.bwtableback.common.exception.ErrorCode;
import com.zero.bwtableback.member.dto.MemberDto;
import com.zero.bwtableback.member.entity.LoginType;
import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.member.entity.Role;
import com.zero.bwtableback.member.oauth2.dto.KakaoUserInfoDto;
import com.zero.bwtableback.member.repository.MemberRepository;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoOAuth2Service {

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${KAKAO_CLIENT_SECRET}")
    private String clientSecret;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    private static final String KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private static final String KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";;

    private final RestTemplate restTemplate;
    private final MemberRepository memberRepository;

    /**
     * 카카오 플랫폼에서 AccessToken 발급
     */
    public String getAccessToken(String code) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("redirect_uri", redirectUri);
        params.add("client_secret", clientSecret);
        params.add("code", code);

        // 요청 엔티티 생성
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            // POST 요청으로 Access Token 받기
            ResponseEntity<String> response = restTemplate.exchange(
                    KAKAO_TOKEN_URL,
                    HttpMethod.POST,
                    request,
                    String.class);

            String responseBody = response.getBody();

            // JSON 응답에서 accessToken 추출
            if (responseBody != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(responseBody);

                String accessToken = jsonNode.path("access_token").asText();

                if (accessToken.isEmpty()) {
                    throw new RuntimeException("응답에서 토큰을 찾을 수 없습니다.");
                }
                return accessToken;
            } else {
                throw new RuntimeException("응답값이 존재하지 않습니다.");
            }
        } catch (
                Exception e) {
            throw new RuntimeException("오류 발생: " + e.getMessage());
        }
    }

    /**
     * 카카오 API로 토큰에 대한 사용자 정보 받기 및 저장
     */
    public MemberDto getUserInfo(String accessToken) throws
            JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                KAKAO_USER_INFO_URL,
                HttpMethod.GET,
                entity, // 요청에 포함될 헤더와 바디 담은 객체
                String.class // 응답 본문 타입
        );

        // JSON 응답을 UserInfo 객체로 변환
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(response.getBody());

        // JsonNode를 Map<String, Object>로 변환
        Map<String, Object> attributes = objectMapper.convertValue(jsonNode, Map.class);

        KakaoUserInfoDto userInfo = new KakaoUserInfoDto(attributes);

        boolean isExistingMember = memberRepository.existsByEmail(userInfo.getEmail());

        if (isExistingMember) {
            throw new CustomException(ErrorCode.USER_ALREADY_EXISTS);
        }

        // 회원 등록 - 소셜 로그인은 비즈앱으로 등록해야 정보를 제공하기에 임시 정보로 대체 (이메일, 전화번호, 이름)
        Member member = Member.builder()
                .loginType(LoginType.SOCIAL)
                .email(userInfo.getEmail())
                .name(userInfo.getName())
                .nickname(userInfo.getNickName())
                .phone(userInfo.getPhone())
                .role(Role.GUEST)
                .provider(userInfo.getProvider())
                .providerId(userInfo.getProviderId())
                .profileImage(userInfo.getProfileImage())
                .build();

        memberRepository.save(member);

        MemberDto memberDto = MemberDto.from(member);

        return memberDto;
    }

    public void kakaoLogout(String accessToken) {
        String url = "https://kapi.kakao.com/v1/user/logout";

        System.out.println(accessToken);

        HttpHeaders headers = new HttpHeaders();

        headers.set("Authorization", "Bearer " + accessToken);
//
        HttpEntity<String> entity = new HttpEntity<>(headers);

        restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        // TODO accessToken, refreshToken 삭제
        // HTTP-only 쿠키에서 액세스 토큰 삭제
        Cookie cookie = new Cookie("accessToken", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/"); // 쿠키가 설정된 경로와 동일하게 설정
        cookie.setMaxAge(0); // 만료 시간을 0으로 설정하여 삭제
        // response.addCookie(cookie);

        // TODO 회원 정보 가져오기
        //String key = "refresh_token:" + memberId; // 삭제할 키 생성
        //redisTemplate.delete(key);
    }
}