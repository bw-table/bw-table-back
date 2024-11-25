package com.zero.bwtableback.member.dto;

import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.member.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class MemberDto {
    private Long id;
    private String email;
    private String name;
    private String nickname;
    private String phone;
    private Role role;
    private String profileImage;
    private String businessNumber; 

    // 손님(GUEST)인 경우를 위한 생성자
    public MemberDto(Long id, String email, String name, String nickname, String phone, Role role, String profileImage) {
        this(id, email, name, nickname, phone, role, profileImage, null);
    }

    public static MemberDto from(Member member) {
        if (member.getRole() == Role.OWNER) {
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
        } else {
            return new MemberDto(
                    member.getId(),
                    member.getEmail(),
                    member.getName(),
                    member.getNickname(),
                    member.getPhone(),
                    member.getRole(),
                    member.getProfileImage()
            );
        }
    }
}
