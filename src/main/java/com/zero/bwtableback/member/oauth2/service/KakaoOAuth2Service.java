package com.zero.bwtableback.member.oauth2.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zero.bwtableback.common.exception.CustomException;
import com.zero.bwtableback.common.exception.ErrorCode;
import com.zero.bwtableback.member.dto.LoginResDto;
import com.zero.bwtableback.member.dto.MemberDto;
import com.zero.bwtableback.member.entity.LoginType;
import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.member.entity.Role;
import com.zero.bwtableback.member.entity.Status;
import com.zero.bwtableback.member.oauth2.dto.KakaoUserInfoDto;
import com.zero.bwtableback.member.repository.MemberRepository;
import com.zero.bwtableback.member.service.AuthService;
import com.zero.bwtableback.restaurant.repository.RestaurantRepository;
import com.zero.bwtableback.security.jwt.TokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    private static final String KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";
    ;

    private final RestTemplate restTemplate;
    private final MemberRepository memberRepository;
    private final RestaurantRepository restaurantRepository;
    private final TokenProvider tokenProvider;
    private final RedisTemplate<String, String> redisTemplate;

    private final AuthService authService;

    /**
     * 카카오 회원 정보를 얻기 위해 카카오 플랫폼에서 AccessToken 발급
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
    public MemberDto getUserInfoAndSignup(String accessToken) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                KAKAO_USER_INFO_URL,
                HttpMethod.GET,
                entity,
                String.class
        );

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(response.getBody());
        Map<String, Object> attributes = objectMapper.convertValue(jsonNode, Map.class);

        KakaoUserInfoDto userInfo = new KakaoUserInfoDto(attributes);

        if (memberRepository.existsByEmail(userInfo.getEmail())) {
            throw new CustomException(ErrorCode.USER_ALREADY_EXISTS);
        }

        return registerNewMember(userInfo);
    }

    private MemberDto registerNewMember(KakaoUserInfoDto userInfo) {
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

        return MemberDto.from(member);
    }

    /**
     * 사용자 로그인을 처리하고 인증 토큰을 반환
     */
    public LoginResDto login(String email, HttpServletRequest request, HttpServletResponse response) {
        // 이메일로 회원 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 탈퇴회원 여부 확인
        if (member.getStatus() == Status.INACTIVE) {
            throw new CustomException(ErrorCode.ALREADY_WITHDRAWN_MEMBER);
        }

        String accessToken = tokenProvider.createAccessToken(member.getEmail(), member.getRole());
        String refreshToken = tokenProvider.createRefreshToken(member.getId().toString());

        // HttpOnly 쿠키에 리프레시 토큰 저장
        authService.saveRefreshTokenToCookie(refreshToken, response);

        // Redis에 리프레시 토큰 저장
        authService.saveRefreshTokenToRedis(member.getId(), refreshToken);

        return new LoginResDto(accessToken, MemberDto.from(member), null);
    }
}