package com.zero.bwtableback.member.dto;

import lombok.Getter;

/**
 * 이메일 로그인 요청을 위한 DTO
 */
@Getter
public class EmailLoginDto {
    private String email;
    private String password;
}
