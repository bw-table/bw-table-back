package com.zero.bwtableback.member.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 이메일 로그인 요청을 위한 DTO
 */
@Getter
@Builder
public class EmailLoginReqDto {
    private String email;
    private String password;
}
