package com.zero.bwtableback.member.oauth2.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.zero.bwtableback.member.oauth2.dto.KakaoUserInfo;
import com.zero.bwtableback.member.oauth2.service.KakaoOAuth2Service;
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

    @GetMapping("/connect")
    public String KakaoConnect() {
        StringBuffer url = new StringBuffer();
        url.append("https://kauth.kakao.com/oauth/authorize?");
        url.append("client_id=" + clientId);
        url.append("&redirect_uri=" + redirectUri);
        url.append("&response_type=code");
        return "redirect:" + url.toString();
    }

    @GetMapping("/callback")
    public ResponseEntity<?> kakaoLogin(@RequestParam String code) throws JsonProcessingException {
        String accessToken = kakaoService.getAccessToken(code);

        KakaoUserInfo userInfo = kakaoService.getUserInfo(accessToken);

        return ResponseEntity.ok(userInfo);
    }

    @GetMapping("/logout")
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