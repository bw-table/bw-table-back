package com.zero.bwtableback.member.dto;

import com.zero.bwtableback.member.entity.LoginType;
import com.zero.bwtableback.member.entity.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupFormDto {
    private LoginType loginType;
    private String email;
    private String name;
    private String password;
    private String nickname;
    private String contactNumber;
    private Role role;
    private String businessNumber;
}
