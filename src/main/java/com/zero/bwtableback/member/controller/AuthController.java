package com.zero.bwtableback.member.controller;

import com.zero.bwtableback.member.dto.*;
import com.zero.bwtableback.member.service.AuthService;
import com.zero.bwtableback.security.MemberDetails;
import com.zero.bwtableback.security.jwt.TokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final TokenProvider tokenProvider;

    /**
     * 이메일 중복 검사
     */
    @PostMapping("/check/email")
    public ResponseEntity<Map<String, Boolean>>  checkEmailDuplicate(@RequestBody DuplicateCheckReqDto request) {
        boolean isDuplicate = authService.isEmailDuplicate(request);
        return ResponseEntity.ok(Collections.singletonMap("isDuplicate", isDuplicate));
    }

    /**
     * 닉네임 중복 검사
     */
    @PostMapping("/check/nickname")
    public ResponseEntity<Map<String, Boolean>> checkNicknameDuplicate(@RequestBody DuplicateCheckReqDto request) {
        boolean isDuplicate = authService.isNicknameDuplicate(request);
        return ResponseEntity.ok(Collections.singletonMap("isDuplicate", isDuplicate));
    }

    /**
     * 전화번호 중복 검사
     */
    @PostMapping("/check/phone")
    public ResponseEntity<Map<String, Boolean>> checkPhoneDuplicate(@RequestBody DuplicateCheckReqDto request) {
        boolean isDuplicate = authService.isPhoneDuplicate(request);
        return ResponseEntity.ok(Collections.singletonMap("isDuplicate", isDuplicate));
    }

    /**
     * 사업자등록번호 중복 검사
     */
    @PostMapping("/check/business-number")
    public ResponseEntity<Map<String, Boolean>> checkBusinessNumberDuplicate(@RequestBody DuplicateCheckReqDto request) {
        boolean isDuplicate = authService.isBusinessNumberDuplicate(request);
        return ResponseEntity.ok(Collections.singletonMap("isDuplicate", isDuplicate));
    }

    /**
     * 회원가입
     */
    @PostMapping("/signup")
    public ResponseEntity<SignUpResDto> signUp(@Valid @RequestBody SignUpReqDto signUpReqDto, BindingResult bindingResult) {
        // 유효성 검사 결과 확인
        if (bindingResult.hasErrors()) {
            StringBuilder errorMessage = new StringBuilder();
            bindingResult.getFieldErrors().forEach(error ->
                    errorMessage.append(error.getDefaultMessage()).append(" ")
            );
        }
        SignUpResDto signUpResDto = authService.signUp(signUpReqDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(signUpResDto);
    }

    /**
     * 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody EmailLoginReqDto loginReqDto,
                                             HttpServletRequest request,
                                             HttpServletResponse response) {
        // 요청 헤더에서 액세스 토큰 추출
        String accessToken = getJwtFromRequest(request);

        // 액세스 토큰이 유효한 경우
        if (StringUtils.hasText(accessToken) && tokenProvider.validateAccessToken(accessToken)) {
            // 기존의 액세스 토큰과 사용자 정보를 반환
            LoginResDto loginResDto = authService.handleExistingToken(accessToken);
            return ResponseEntity.ok(loginResDto);
        }

        LoginResDto loginResDto = authService.login(loginReqDto, request, response);
        return ResponseEntity.ok(loginResDto);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " 부분을 제거하고 토큰 반환
        }
        return null; // 토큰이 없으면 null 반환
    }
    /**
     * 리프레시 토큰을 사용하여 액세스 토큰 발급
     */
    @PostMapping("/refresh")
    public ResponseEntity<LoginResDto> refresh(HttpServletRequest request) {
        String refreshToken = getRefreshTokenFromCookies(request); // 쿠키에서 리프레시 토큰 추출
        LoginResDto loginResDto = authService.renewAccessTokenWithRefreshToken(refreshToken);
        return ResponseEntity.ok(loginResDto);
    }

    private String getRefreshTokenFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null; // 쿠키가 없으면 null 반환
    }

    /**
     * 로그아웃 처리
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal MemberDetails memberDetails) {
        String email = memberDetails.getUsername();
        authService.logout(email);
        return ResponseEntity.noContent().build();
    }
}
