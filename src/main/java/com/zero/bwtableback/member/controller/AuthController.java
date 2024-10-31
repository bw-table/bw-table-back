package com.zero.bwtableback.member.controller;

import com.zero.bwtableback.common.response.ApiResponse;
import com.zero.bwtableback.member.dto.EmailLoginDto;
import com.zero.bwtableback.member.dto.SignUpDto;
import com.zero.bwtableback.member.dto.TokenDto;
import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.member.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 회원가입
     */
    @PostMapping("/signup")
    public ApiResponse<Member> signUp(@RequestBody SignUpDto signUpDto) {
        try {
            Member member = authService.signUp(signUpDto);
            return ApiResponse.success(member);
        } catch (Exception e) {
            return ApiResponse.error("INTERNAL_SERVER_ERROR", "회원가입에 실패했습니다: ", e.getMessage());
        }
    }

    /**
     * 로그인
     */
    @PostMapping("/login")
    public ApiResponse<TokenDto> login(@RequestBody EmailLoginDto loginDto,
                                       HttpServletResponse response) {
        try {
            TokenDto tokenDto = authService.login(loginDto);

            // HttpOnly 쿠키에 리프레시 토큰 저장
            Cookie cookie = new Cookie("refreshToken", tokenDto.getRefreshToken());
            cookie.setHttpOnly(true); // JavaScript 접근 불가
            cookie.setSecure(true); // HTTPS에서만 전송
            cookie.setPath("/"); // 쿠키의 유효 경로 설정
            cookie.setMaxAge(86400); // 1일 (초 단위)

            response.addCookie(cookie); // 쿠키를 응답에 추가

            return ApiResponse.success(tokenDto);
        } catch (Exception e) {
            return ApiResponse.error("INTERNAL_SERVER_ERROR", "로그인에 실패했습니다.", e.getMessage());
        }
    }

    /**
     * 리프레시 토큰을 사용하여 새로운 액세스 토큰 발급
     */
    @PostMapping("/refresh")
    public ApiResponse<TokenDto> refreshToken(@RequestParam String refreshToken) {
        try {
            TokenDto tokenDto = authService.refreshToken(refreshToken);
            return ApiResponse.success(tokenDto);
        } catch (Exception e) {
            return ApiResponse.error("INTERNAL_SERVER_ERROR", "토큰 갱신에 실패했습니다: ", e.getMessage());
        }
    }

    /**
     * 로그아웃 처리
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestParam String email) {
        try {
            authService.logout(email);
            return ApiResponse.success();
        } catch (Exception e) {
            return ApiResponse.error("INTERNAL_SERVER_ERROR", "로그아웃에 실패했습니다: ", e.getMessage());
        }
    }
}
