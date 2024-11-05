package com.zero.bwtableback.member.oauth2.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zero.bwtableback.member.entity.LoginType;
import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.member.entity.Role;
import com.zero.bwtableback.member.oauth2.dto.KakaoUserInfo;
import com.zero.bwtableback.member.repository.MemberRepository;
import com.zero.bwtableback.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoOAuth2Service {

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${KAKAO_CLIENT_SECRET}")
    String clientSecret;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    private String kakaoTokenUrl = "https://kauth.kakao.com/oauth/token";
    private String kakaoUserInfoUrl = "https://kapi.kakao.com/v2/user/me";

    private final RestTemplate restTemplate;
    private final MemberService memberService;
    private final MemberRepository memberRepository;

    public String getAccessToken(String code) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", "a55636a5224c30f8eec17b6320261cf9");
        params.add("redirect_uri", "http://localhost:8080/api/oauth2/kakao/callback");
        params.add("client_secret", "Fl0mGkaFBBI0h8n9igeU6ndceYgjb91T");
        params.add("code", code);

        // 요청 엔티티 생성
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        System.out.println("응답 전");
        System.out.println(request);

        try {
            // POST 요청으로 Access Token 받기
            ResponseEntity<String> response = restTemplate.exchange(
                    kakaoTokenUrl,
                    HttpMethod.POST,
                    request,
                    String.class);

            System.out.println("응답 후");
            String responseBody = response.getBody();

            // JSON 응답에서 access_token 추출
            if (responseBody != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> responseMap = objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {
                });

                if (responseMap.containsKey("access_token")) {
                    return (String) responseMap.get("access_token");
                } else {
                    throw new RuntimeException("Access token not found in the response");
                }
            } else {
                throw new RuntimeException("Response body is null");
            }
        } catch (HttpClientErrorException e) {
            System.err.println("HTTP 상태 코드: " + e.getStatusCode());
            System.err.println("응답 본문: " + e.getResponseBodyAsString());
            throw new RuntimeException("액세스 토큰을 얻는 데 실패했습니다: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("예기치 않은 오류 발생: " + e.getMessage());
        }
    }

    public KakaoUserInfo getUserInfo(String accessToken) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                kakaoUserInfoUrl,
                HttpMethod.GET,
                entity, // 요청에 포함될 헤더와 바디 담은 객체
                String.class // 응답 본문 타입
        );

        // JSON 응답을 UserInfo 객체로 변환
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(response.getBody());

        // JsonNode를 Map<String, Object>로 변환
        Map<String, Object> attributes = objectMapper.convertValue(jsonNode, Map.class);

        System.out.println(attributes);

        KakaoUserInfo userInfo = new KakaoUserInfo(attributes);

        boolean isExistingMember = memberRepository.existsByEmail(userInfo.getEmail());
        System.out.println(userInfo.getEmail());
        if (!isExistingMember) {
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
        }

        return userInfo;
    }

    public void kakaoLogout(String accessToken) {
    }
}