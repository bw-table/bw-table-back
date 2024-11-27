package com.zero.bwtableback.member.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DuplicateCheckReqDto {
    private String email;
    private String nickname;
    private String phone;
    private String businessNumber;
}
