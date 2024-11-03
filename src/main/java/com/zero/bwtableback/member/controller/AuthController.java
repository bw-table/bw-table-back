package com.zero.bwtableback.member.controller;

import com.zero.bwtableback.member.dto.EmailLoginReqDto;
import com.zero.bwtableback.member.dto.SignUpReqDto;
import com.zero.bwtableback.member.dto.SignUpResDto;
import com.zero.bwtableback.member.dto.TokenDto;
import com.zero.bwtableback.member.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * 회원가입
     */
    @PostMapping("/signup")
    public ResponseEntity<SignUpResDto> signUp(@RequestBody SignUpReqDto signUpReqDto) {
        SignUpResDto responseDto = authService.signUp(signUpReqDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    /**
     * 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<TokenDto> login(@RequestBody EmailLoginReqDto loginDto,
                                          HttpServletResponse response) {
        TokenDto tokenDto = authService.login(loginDto);

        // HttpOnly 쿠키에 리프레시 토큰 저장
        Cookie cookie = new Cookie("refreshToken", tokenDto.getRefreshToken());
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(86400);

        response.addCookie(cookie);

        return ResponseEntity.ok(tokenDto);
    }

    /**
     * 리프레시 토큰을 사용하여 새로운 액세스 토큰 발급
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenDto> refreshToken(@RequestParam String refreshToken) {
        TokenDto tokenDto = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(tokenDto);
    }

    /**
     * 로그아웃 처리
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestParam String email) {
        authService.logout(email);
        return ResponseEntity.noContent().build();
    }
}
