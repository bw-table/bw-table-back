package com.zero.bwtableback.member.dto;

import com.zero.bwtableback.member.entity.LoginType;
import com.zero.bwtableback.member.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class SignUpReqDto {
    private LoginType loginType;
    private Role role;
    private String email;
    private String name;
    private String password;
    private String nickname;
    private String phone;
    private String businessNumber;
}
