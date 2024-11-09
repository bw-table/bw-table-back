package com.zero.bwtableback.member.oauth2.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.zero.bwtableback.member.dto.LoginResDto;
import com.zero.bwtableback.member.dto.MemberDto;
import com.zero.bwtableback.member.oauth2.service.KakaoOAuth2Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/callback")
    @Operation(summary = "카카오 로그인", description = "카카오 로그인 후 사용자 정보를 반환합니다.")
    @ApiResponses(value = { // FIXME Swagger UI 예시
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })

    public ResponseEntity<LoginResDto> kakaoLogin(@RequestParam String code,
                                                  HttpServletRequest request,
                                                  HttpServletResponse response) throws JsonProcessingException {
        // 카카오 토큰 발급 및 저장
        String kakaoToken = kakaoService.getAccessToken(code);
        MemberDto memberDto = kakaoService.getUserInfoAndSignup(kakaoToken);

        LoginResDto loginResDto = kakaoService.login(memberDto, request, response);

        return ResponseEntity.ok(loginResDto);
    }

    @DeleteMapping("/logout")
    @Operation(summary = "카카오 로그아웃", description = "사용자를 카카오에서 로그아웃 처리합니다.")
    public ResponseEntity<?> kakaoLogout(@RequestHeader("Authorization")
                                         String accessToken,
                                         HttpServletResponse response) {
        kakaoService.kakaoLogout(accessToken, response);
        return ResponseEntity.noContent().build();
    }
}