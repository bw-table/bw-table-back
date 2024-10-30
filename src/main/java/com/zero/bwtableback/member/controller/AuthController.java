package com.zero.bwtableback.member.controller;

import com.zero.bwtableback.member.dto.EmailLoginDto;
import com.zero.bwtableback.member.dto.SignUpDto;
import com.zero.bwtableback.member.dto.TokenDto;
import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.member.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 회원가입
     */
    @PostMapping("/signup")
    public ResponseEntity<Member> signUp(@RequestBody SignUpDto signUpDto) {
        Member member = authService.signUp(signUpDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(member);
    }

    /**
     * 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<TokenDto> login(@RequestBody EmailLoginDto loginDto) {
        TokenDto tokenDto = authService.login(loginDto);
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
