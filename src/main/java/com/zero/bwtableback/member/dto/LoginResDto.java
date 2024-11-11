package com.zero.bwtableback.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResDto {
    String accessToken;
    MemberDto member;
    Long restaurantId;
}
