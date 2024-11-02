package com.zero.bwtableback.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * JWT와 리프레시 토큰을 포함한 응답 DTO
 */
@Getter
@Setter
@AllArgsConstructor
public class TokenDto {
    private String accessToken;
    private String refreshToken;
}
