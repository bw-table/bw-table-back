package com.zero.bwtableback.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DuplicateCheckReqDto {
    private String email;
    private String nickname;
    private String phone;
    private String businessNumber;
}
