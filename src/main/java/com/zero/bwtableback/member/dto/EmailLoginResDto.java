package com.zero.bwtableback.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EmailLoginResDto {
    private String accessToken;
    private MemberDto member;
}
