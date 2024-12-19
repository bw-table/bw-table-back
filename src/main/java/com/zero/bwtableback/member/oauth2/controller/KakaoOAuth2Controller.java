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
    @GetMapping("/callback")
    @Operation(summary = "카카오 로그인 및 회원가입", description = "카카오 회원가입 및 로그인 후 사용자 정보를 반환합니다.")
    public ResponseEntity<?> kakaoLogin(@RequestParam(required = false) String code,
                                        HttpServletRequest request,
                                        HttpServletResponse response) throws JsonProcessingException {
        // 첫 번째 로그인: 카카오에서 정보를 추출하여 서버에 회원가입
        if (code != null) {
            String kakaoToken = kakaoService.getAccessToken(code);
            MemberDto memberDto = kakaoService.getUserInfoAndSignup(kakaoToken);
            LoginResDto loginResDto = kakaoService.login(memberDto.getEmail(), request, response);

            return ResponseEntity.ok(loginResDto);
        } else {
            // 카카오 인가 코드가 없는 경우
            String accessToken = tokenProvider.extractToken(request);
            if (StringUtils.hasText(accessToken)) {
                if (tokenProvider.validateAccessToken(accessToken)) {
                    // 유효한 액세스 토큰이 있는 경우
                    LoginResDto loginResDto = authService.handleExistingToken(accessToken);
                    return ResponseEntity.ok(loginResDto);
                } else {
                    // 액세스 토큰이 만료된 경우
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body("토큰이 만료되었습니다.");
                }
            } else {
                // 액세스 토큰이 없는 경우
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("토큰이 존재하지 않습니다.");
            }
        }
    }
}