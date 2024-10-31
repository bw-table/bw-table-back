package com.zero.bwtableback.member.controller;

import com.zero.bwtableback.common.response.ApiResponse;
import com.zero.bwtableback.member.dto.EmailLoginDto;
import com.zero.bwtableback.member.dto.SignUpDto;
import com.zero.bwtableback.member.dto.TokenDto;
import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.member.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
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
     * 이메일 중복 검사
     */
    @PostMapping("/check/email")
    public ApiResponse<Boolean> checkEmailDuplicate(@RequestBody String email) {
        try {
            boolean isDuplicate = authService.isEmailDuplicate(email);
            return ApiResponse.success(isDuplicate);
        } catch (Exception e) {
            return ApiResponse.error("EMAIL_CHECK_ERROR", "이메일 중복 확인 중 오류가 발생했습니다.", e.getMessage());
        }
    }

    /**
     * 닉네임 중복 검사
     */
    @PostMapping("/check/nickname")
    public ApiResponse<Boolean> checkNicknameDuplicate(@RequestBody String nickname) {
        try {
            boolean isDuplicate = authService.isNicknameDuplicate(nickname);
            return ApiResponse.success(isDuplicate);
        } catch (Exception e) {
            return ApiResponse.error("NICKNAME_CHECK_ERROR", "닉네임 중복 확인 중 오류가 발생했습니다.", e.getMessage());
        }
    }

    /**
     * 전화번호 중복 검사
     */
    @PostMapping("/check/phone")
    public ApiResponse<Boolean> checkPhoneDuplicate(@RequestBody String phone) {
        try {
            //TODO String cleanPhone = PhoneNumberUtil.removeHyphens(phone);
            boolean isDuplicate = authService.isPhoneDuplicate(phone);
            return ApiResponse.success(isDuplicate);
        } catch (Exception e) {
            return ApiResponse.error("PHONE_CHECK_ERROR", "전화번호 중복 확인 중 오류가 발생했습니다.", e.getMessage());
        }
    }

    /**
     * 사업자등록번호 중복 검사
     */
    @PostMapping("/check/business-number")
    public ApiResponse<Boolean> checkBusinessNumberDuplicate(@RequestBody String businessNumber) {
        try {
            //TODO String cleanBusinessNumber = BusinessNumberUtil.removeHyphens(businessNumber);
            boolean isDuplicate = authService.isBusinessNumberDuplicate(businessNumber);
            return ApiResponse.success(isDuplicate);
        } catch (Exception e) {
            return ApiResponse.error("BUSINESS_NUMBER_CHECK_ERROR", "사업자 등록번호 중복 확인 중 오류가 발생했습니다.", e.getMessage());
        }
    }

    /**
     * 회원가입
     */
    @PostMapping("/signup")
    public ApiResponse<Member> signUp(@Valid @RequestBody SignUpDto signUpDto, BindingResult bindingResult) {
        // 유효성 검사 결과 확인
        if (bindingResult.hasErrors()) {
            // 에러 메시지를 수집하여 ApiResponse에 담아 반환
            StringBuilder errorMessage = new StringBuilder();
            bindingResult.getFieldErrors().forEach(error ->
                    errorMessage.append(error.getDefaultMessage()).append(" ")
            );
            return ApiResponse.error("VALIDATION_ERROR", "회원가입에 실패했습니다. ", errorMessage.toString().trim());
        }

        try {
            Member member = authService.signUp(signUpDto);
            return ApiResponse.success(member);
        } catch (Exception e) {
            return ApiResponse.error("INTERNAL_SERVER_ERROR", "회원가입에 실패했습니다. ", e.getMessage());
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
            return ApiResponse.error("INTERNAL_SERVER_ERROR", "토큰 갱신에 실패했습니다. ", e.getMessage());
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
            return ApiResponse.error("INTERNAL_SERVER_ERROR", "로그아웃에 실패했습니다. ", e.getMessage());
        }
    }
}
