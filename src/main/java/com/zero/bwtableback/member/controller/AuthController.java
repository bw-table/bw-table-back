package com.zero.bwtableback.member.controller;

import com.zero.bwtableback.member.dto.EmailLoginReqDto;
import com.zero.bwtableback.member.dto.SignUpReqDto;
import com.zero.bwtableback.member.dto.SignUpResDto;
import com.zero.bwtableback.member.dto.TokenDto;
import com.zero.bwtableback.member.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * 이메일 중복 검사
     */
    @PostMapping("/check/email")
    public ResponseEntity<Boolean> checkEmailDuplicate(@RequestBody String email) {
            boolean isDuplicate = authService.isEmailDuplicate(email);
            return ResponseEntity.ok(isDuplicate);

    }

    /**
     * 닉네임 중복 검사
     */
    @PostMapping("/check/nickname")
    public ResponseEntity<Boolean> checkNicknameDuplicate(@RequestBody String nickname) {
            boolean isDuplicate = authService.isNicknameDuplicate(nickname);
            return ResponseEntity.ok(isDuplicate);

    }

    /**
     * 전화번호 중복 검사
     */
    @PostMapping("/check/phone")
    public ResponseEntity<Boolean> checkPhoneDuplicate(@RequestBody String phone) {
            //TODO String cleanPhone = PhoneNumberUtil.removeHyphens(phone);
            boolean isDuplicate = authService.isPhoneDuplicate(phone);
            return ResponseEntity.ok(isDuplicate);

    }

    /**
     * 사업자등록번호 중복 검사
     */
    @PostMapping("/check/business-number")
    public ResponseEntity<Boolean> checkBusinessNumberDuplicate(@RequestBody String businessNumber) {
            //TODO String cleanBusinessNumber = BusinessNumberUtil.removeHyphens(businessNumber);
            boolean isDuplicate = authService.isBusinessNumberDuplicate(businessNumber);
            return ResponseEntity.ok(isDuplicate);
    }

    /**
     * 회원가입
     */
    @PostMapping("/signup")
    public ResponseEntity<SignUpResDto> signUp(@Valid @RequestBody SignUpReqDto signUpReqDto, BindingResult bindingResult) {
        // 유효성 검사 결과 확인
        if (bindingResult.hasErrors()) {
            // 에러 메시지를 수집하여 ResponseEntity에 담아 반환
            StringBuilder errorMessage = new StringBuilder();
            bindingResult.getFieldErrors().forEach(error ->
                    errorMessage.append(error.getDefaultMessage()).append(" ")
            );
        }

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
    public ResponseEntity<Void> logout() {
        authService.logout();
        return ResponseEntity.noContent().build();
    }
}
