package com.zero.bwtableback.member.dto;

import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.member.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberDto {
    private Long id;
    private String email;
    private String name;
    private String nickname;
    private String phone;
    private Role role;
    private String profileImage;
    private String businessNumber; // OWNER 역할일 경우에만

    public static MemberDto from(Member member) {
        return new MemberDto(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getNickname(),
                member.getPhone(),
                member.getRole(),
                member.getProfileImage(),
                member.getBusinessNumber()
        );
    }
}
