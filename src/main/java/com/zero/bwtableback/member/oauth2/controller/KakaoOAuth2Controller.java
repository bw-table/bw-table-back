package com.zero.bwtableback.member.oauth2.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.zero.bwtableback.common.exception.CustomException;
import com.zero.bwtableback.member.dto.LoginResDto;
import com.zero.bwtableback.member.dto.MemberDto;
import com.zero.bwtableback.member.oauth2.service.KakaoOAuth2Service;
import com.zero.bwtableback.member.service.AuthService;
import com.zero.bwtableback.security.jwt.TokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/oauth2/kakao")
public class KakaoOAuth2Controller {
    @Value("${KAKAO_CLIENT_ID}")
    String clientId;
    @Value("${KAKAO_CLIENT_SECRET}")
    String clientSecret;
    @Value("${KAKAO.REDIRECT_URI}")
    String redirectUri;

    private final KakaoOAuth2Service kakaoService;
    private final AuthService authService;
    private final TokenProvider tokenProvider;

    /**
     * 카카오 로그인 및 회원가입 처리
     */
    @PostMapping("/callback")
    @Operation(summary = "카카오 로그인 및 회원가입", description = "카카오 회원가입 및 로그인 후 사용자 정보를 반환합니다.")
    public ResponseEntity<?> kakaoLogin(@RequestParam(required = false) String code,
                                                  HttpServletRequest request,
                                                  HttpServletResponse response) throws JsonProcessingException {
        // 요청 헤더에서 액세스 토큰 추출
        String accessToken = getJwtFromRequest(request);

        // 첫 번째 로그인: 카카오에서 정보를 추출하여 서버에 회원가입
        if (code != null) {
            String kakaoToken = kakaoService.getAccessToken(code);
            MemberDto memberDto = kakaoService.getUserInfoAndSignup(kakaoToken);
            LoginResDto loginResDto = kakaoService.login(memberDto.getEmail(), request, response);

            return ResponseEntity.ok(loginResDto);
        } else {
            // 액세스 토큰 존재하고 유효한 경우
            if (StringUtils.hasText(accessToken) && tokenProvider.validateAccessToken(accessToken)) {
                // 기존의 액세스 토큰과 사용자 정보를 반환
                LoginResDto loginResDto = authService.handleExistingToken(accessToken);

                return ResponseEntity.ok(loginResDto);
            }
            // 토큰이 없거나 유효하지 않은 경우 401 응답을 던짐
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Unauthorized. 토큰이 유효하지 않습니다.");
        }
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " 부분을 제거하고 토큰 반환
        }
        return null; // 토큰이 없으면 null 반환
    }
}