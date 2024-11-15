package com.zero.bwtableback.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * JWT와 리프레시 토큰을 포함한 응답 DTO
 */
@Getter
@AllArgsConstructor
public class TokenDto {
    private String accessToken;
    private String refreshToken;

    // 기본 생성자 추가 (선택 사항)
    public TokenDto(String accessToken) {
        this.accessToken = accessToken;
        this.refreshToken = null; // 기본값 설정
    }
}
