package com.zero.bwtableback.member.controller;

import com.zero.bwtableback.common.exception.CustomException;
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
    public ResponseEntity<Map<String, Boolean>> checkEmailDuplicate(@RequestBody DuplicateCheckReqDto request) {
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
     * 회원가입 및 자동 로그인
     */
    @PostMapping("/signup")
    public ResponseEntity<LoginResDto> signUp(@Valid @RequestBody SignUpReqDto signUpReqDto,
                                              HttpServletRequest request,
                                              HttpServletResponse response,
                                              BindingResult bindingResult) {
        // 유효성 검사 결과 확인
        if (bindingResult.hasErrors()) {
            StringBuilder errorMessage = new StringBuilder();
            bindingResult.getFieldErrors().forEach(error ->
                    errorMessage.append(error.getDefaultMessage()).append(" ")
            );
        }
        MemberDto memberDto = authService.signUp(signUpReqDto);

        LoginResDto loginResDto = authService.signUpLogin(memberDto, request, response);

        return ResponseEntity.status(HttpStatus.CREATED).body(loginResDto);
    }

    /**
     * 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody EmailLoginReqDto loginReqDto,
                                   HttpServletRequest request,
                                   HttpServletResponse response) {
        try {
            MemberDto authenticatedMember = authService.authenticateMember(loginReqDto);
            String accessToken = tokenProvider.extractToken(request);

            if (StringUtils.hasText(accessToken)) {
                if (tokenProvider.validateAccessToken(accessToken)) {
                    // 유효한 토큰이 있는 경우, 기존 토큰 정보 반환
                    return ResponseEntity.ok(authService.handleExistingToken(accessToken));
                } else {
                    // 토큰이 만료된 경우, 401 에러 반환
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("토큰이 만료되었습니다.");
                }
            } else {
                // 토큰이 없는 경우, 새로운 로그인 프로세스 진행
                return ResponseEntity.ok(authService.login(authenticatedMember, request, response));
            }
        } catch (CustomException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Forbidden: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 정보가 유효하지 않습니다.");
        }
    }

    /**
     * 리프레시 토큰을 사용하여 액세스 토큰 발급
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request) {
        String refreshToken = getRefreshTokenFromCookies(request);
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Unauthorized. 토큰이 유효하지 않습니다.");
        }
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
        return null;
    }

    /**
     * 로그아웃 처리
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@AuthenticationPrincipal MemberDetails memberDetails,
                                    HttpServletResponse response) {
        String email = memberDetails.getUsername();

        authService.logout(email, response);

        return ResponseEntity.ok("로그아웃이 완료되었습니다.");
    }

    /**
     * 회원 탈퇴
     * - 토큰 정보 삭제
     */
    @DeleteMapping("/withdraw")
    public ResponseEntity<?> withdrawMember(@AuthenticationPrincipal MemberDetails memberDetails,
                                            HttpServletResponse response) {
        authService.withdraw(memberDetails.getMemberId(), response);

        return ResponseEntity.ok().body("회원탈퇴가 완료되었습니다.");
    }

}
