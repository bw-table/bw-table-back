package com.zero.bwtableback.member.dto;

import com.zero.bwtableback.member.entity.LoginType;
import com.zero.bwtableback.member.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public class MemberPrivateDto extends MemberDto {
    private LoginType loginType;

    public MemberPrivateDto(Long id, String email, String name, String nickname,
                            String phone, Role role, String profileImage, String businessNubmer,
                            LoginType loginType) {
        super(id, email, name, nickname, phone, role, profileImage, businessNubmer);
        this.loginType = loginType;
    }
}
