package com.zero.bwtableback.member.dto;

import com.zero.bwtableback.member.entity.LoginType;
import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.member.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SignUpResDto {
    private Long id;
    private String email;
    private String name;
    private String nickname;
    private String phone;
    private LoginType loginType;
    private Role role;

    public static SignUpResDto from(Member member) {
        return new SignUpResDto(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getNickname(),
                member.getPhone(),
                member.getLoginType(),
                member.getRole()
        );
    }
}
