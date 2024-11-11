package com.zero.bwtableback.member.dto;

import lombok.Getter;

@Getter
public class DuplicateCheckReqDto {
    private String email;
    private String nickname;
    private String phone;
    private String businessNumber;
}
