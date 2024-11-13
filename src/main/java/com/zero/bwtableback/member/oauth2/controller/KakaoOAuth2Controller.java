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

    public ResponseEntity<LoginResDto> kakaoLogin(@RequestParam(required = false) String code,
                                                  HttpServletRequest request,
                                                  HttpServletResponse response) throws JsonProcessingException {
        // 첫 번째 로그인: 카카오에서 정보를 추출하여 서버에 회원가입
        if (code != null) {
            String kakaoToken = kakaoService.getAccessToken(code);
            MemberDto memberDto = kakaoService.getUserInfoAndSignup(kakaoToken);
            LoginResDto loginResDto = kakaoService.login(memberDto, request, response);

            return ResponseEntity.ok(loginResDto);
        } else {
            // 두 번째 이후 로그인: 기존 액세스 토큰으로 처리
            MemberDto memberDto = kakaoService.getMemberInfo(request);
            LoginResDto loginResDto = kakaoService.login(memberDto, request, response);

            return ResponseEntity.ok(loginResDto);
        }
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