package com.zero.bwtableback.member.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 이메일 로그인 요청을 위한 DTO
 */
@Getter
@Setter
public class EmailLoginDto {
    private String email;
    private String password;
}
