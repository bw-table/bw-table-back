package com.zero.bwtableback.member.oauth2.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.member.oauth2.dto.KakaoLoginResDto;
import com.zero.bwtableback.member.oauth2.service.KakaoOAuth2Service;
import com.zero.bwtableback.security.jwt.TokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

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
    private final TokenProvider tokenProvider;

    @GetMapping("/callback")
    @Operation(summary = "카카오 로그인", description = "카카오 로그인 후 사용자 정보를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    public ResponseEntity<KakaoLoginResDto> kakaoLogin(@RequestParam String code) throws JsonProcessingException {
        // 카카오 토큰 생성
        String kakaoToken = kakaoService.getAccessToken(code);
        Member member = kakaoService.getUserInfo(kakaoToken);

        // 자체 토큰 생성
        String accessToken = tokenProvider.createAccessToken(member.getEmail());
        String refreshToken = tokenProvider.createRefreshToken();

        kakaoService.saveRefreshTokenAndCreateCookie(member.getId(),refreshToken);

        KakaoLoginResDto response = new KakaoLoginResDto(
                accessToken,
                member
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/logout")
    @Operation(summary = "카카오 로그아웃", description = "사용자를 카카오에서 로그아웃 처리합니다.")
    public ResponseEntity<?> kakaoLogout(@RequestHeader("Authorization") String accessToken) {
        System.out.println(accessToken);
        kakaoService.kakaoLogout(accessToken);
        return ResponseEntity.noContent().build();
    }

    // TODO 엑세스 토큰 검증
    // TODO 엑세스 토큰 만료 시
}