package com.zero.bwtableback.member.oauth2.dto;


import com.zero.bwtableback.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class KakaoLoginResDto {
    private String accessToken;
    private Member member;
}
