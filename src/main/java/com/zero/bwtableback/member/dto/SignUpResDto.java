package com.zero.bwtableback.member.dto;

import com.zero.bwtableback.member.entity.LoginType;
import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.member.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class SignUpResDto {
    private String email;
    private String name;
    private String nickname;
    private String phone;
    private LoginType loginType;
    private Role role;
    private LocalDateTime createdAt;

    public static SignUpResDto from(Member member) {
        return new SignUpResDto(
                member.getEmail(),
                member.getName(),
                member.getNickname(),
                member.getPhone(),
                member.getLoginType(),
                member.getRole(),
                member.getCreatedAt()
        );
    }
}
