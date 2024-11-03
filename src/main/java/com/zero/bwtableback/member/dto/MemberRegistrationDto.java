package com.zero.bwtableback.member.dto;

import com.zero.bwtableback.member.entity.Role;

public class MemberRegistrationDto {
    private String email;
    private String password;
    private String name;
    private String nickname;
    private String contactNumber;
    private Role role;
    private String businessNumber; // OWNER 역할일 때만 사용
}
