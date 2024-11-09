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
import com.zero.bwtableback.member.oauth2.dto.KakaoUserInfoDto;
import com.zero.bwtableback.member.repository.MemberRepository;
import com.zero.bwtableback.security.jwt.TokenProvider;
import jakarta.servlet.http.Cookie;
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
    private final TokenProvider tokenProvider;
    private final RedisTemplate<String, String> redisTemplate;

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
        KakaoUserInfoDto userInfo = getUserInfoFromKakao(accessToken);

        if (memberRepository.existsByEmail(userInfo.getEmail())) {
            throw new CustomException(ErrorCode.USER_ALREADY_EXISTS);
        }

        return registerNewMember(userInfo);
    }

    private KakaoUserInfoDto getUserInfoFromKakao(String accessToken) throws JsonProcessingException {
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

        return new KakaoUserInfoDto(attributes);
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
    public LoginResDto login(MemberDto memberDto, HttpServletRequest request, HttpServletResponse response) {
        String existingToken = tokenProvider.extractToken(request);

        // 기존 토큰이 존재하는 경우 유효한 경우
        if (existingToken != null && tokenProvider.validateToken(existingToken)) {
            return handleExistingToken(existingToken);
        }


        // 기존 토큰이 존재하지만 유효하지 않은 경우
        if (existingToken != null && !tokenProvider.validateToken(existingToken)) {
            // 리프레시 토큰 확인하고 accessToken 반환
            String refreshToken = getRefreshTokenFromRequest(request); // 요청에서 리프레시 토큰 추출

            if (refreshToken != null && tokenProvider.validateToken(refreshToken)) {
                // 리프레시 토큰이 유효한 경우 새로운 액세스 토큰 생성
                String email = tokenProvider.getUsername(refreshToken); // 이메일 추출
                return handleValidRefreshToken(email, response);
            } else {
                throw new CustomException(ErrorCode.INVALID_TOKEN);
            }
        }

        return handleNewLogin(memberDto, response);
    }

    // 기존 유효한 AccessToken이 존재하는 경우
    private LoginResDto handleExistingToken(String existingToken) {
        String email = tokenProvider.getUsername(existingToken);
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        MemberDto memberDto = MemberDto.from(member);

        return new LoginResDto(existingToken, memberDto);
    }

    // 새로운 로그인 처리 - 첫 로그인 시, 토큰 없을 시, 토큰이 만료된 경우
    private LoginResDto handleNewLogin(MemberDto memberDto, HttpServletResponse response) {
        Member member = memberRepository.findByEmail(memberDto.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 토큰 생성
        String accessToken = tokenProvider.createAccessToken(member.getEmail());
        String refreshToken = tokenProvider.createRefreshToken();

        // HttpOnly 쿠키에 리프레시 토큰 저장
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // HTTPS 환경에서만 전송
        cookie.setPath("/");
        cookie.setMaxAge(86400); // 1일 (86400초)

        response.addCookie(cookie);

        // 리프레시 토큰을 레디스에 저장
        String key = "refresh_token:" + member.getId();
        redisTemplate.opsForValue().set(key, refreshToken);

        return new LoginResDto(accessToken, memberDto);
    }

    // 리프레시 토큰을 사용하여 새로운 액세스 토큰 발급
    // Accesstoken이 만료되거나 없는경우 Refresh 토큰이 유효한 경우
    private LoginResDto handleValidRefreshToken(String email, HttpServletResponse response) {
        // 사용자 정보를 데이터베이스에서 가져오기
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String newAccessToken = tokenProvider.createAccessToken(member.getEmail());

        MemberDto memberDto = MemberDto.from(member);

        return new LoginResDto(newAccessToken, memberDto);
    }

    // 요청에서 리프레시 토큰 추출
    private String getRefreshTokenFromRequest(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public void kakaoLogout(String accessToken, HttpServletResponse response) {
        String url = "https://kapi.kakao.com/v1/user/logout";

        // FIXME accessToken은 카카오 토큰이 필요
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            // 상태 코드 확인
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                // 로그아웃 성공 시 처리

                // HTTP-only 쿠키에서 액세스 토큰 삭제
                Cookie cookie = new Cookie("refreshToken", null);
                cookie.setHttpOnly(true);
                cookie.setPath("/");
                cookie.setMaxAge(0);
                response.addCookie(cookie);

                // TODO AccessToken 및 RefreshToken 삭제 로직 추가
                // redisTemplate.delete("refresh_token:" + memberId);
            } else {
                // TODO 예외 처리
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}