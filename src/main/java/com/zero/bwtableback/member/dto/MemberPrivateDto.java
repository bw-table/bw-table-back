package com.zero.bwtableback.member.dto;

import com.zero.bwtableback.member.entity.LoginType;
import com.zero.bwtableback.member.entity.Member;
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

    public static MemberPrivateDto from(Member member) {
        return new MemberPrivateDto(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getNickname(),
                member.getPhone(),
                member.getRole(),
                member.getProfileImage(),
                member.getBusinessNumber(),
                member.getLoginType()
        );
    }
}
