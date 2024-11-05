package com.zero.bwtableback.member.oauth2.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.member.oauth2.dto.KakaoLoginResDto;
import com.zero.bwtableback.member.oauth2.service.KakaoOAuth2Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    public ResponseEntity<KakaoLoginResDto> kakaoLogin(@RequestParam String code) throws JsonProcessingException {
        String accessToken = kakaoService.getAccessToken(code);

        // TODO Refresh 토큰 받아오기
        String refreshToken = accessToken;
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(86400);

        Member member = kakaoService.getUserInfo(accessToken);

        KakaoLoginResDto response = new KakaoLoginResDto(
                accessToken,
                member
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/logout")
    @Operation(summary = "카카오 로그아웃", description = "사용자를 카카오에서 로그아웃 처리합니다.")
    public String kakaoLogout(HttpSession session) {
        String accessToken = (String) session.getAttribute("kakaoToken");

        if (accessToken != null) {
            kakaoService.kakaoLogout(accessToken);
            session.removeAttribute("kakaoToken"); // 세션에서 토큰 제거
            session.invalidate(); // 세션 무효화
        }

        return "redirect:/";
    }
}