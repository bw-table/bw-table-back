package com.zero.bwtableback.member.entity;

import com.zero.bwtableback.common.BaseEntity;
import com.zero.bwtableback.member.dto.SignUpReqDto;
import jakarta.persistence.*;
import lombok.*;

import java.util.Locale;

@Entity
@Getter
@Setter //FIXME redis 구성 시 삭제
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "member")
public class Member extends BaseEntity {

    @Id
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoginType loginType; // SOCIAL, EMAIL

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = true)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private String phone; // 연락처

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; // 역할 (GUEST(손님), OWNER(사장님))

    @Column(name = "business_number")
    private String businessNumber; // 사업자등록번호 (예시:"123-01-11111")

    @Column(name = "profile_image_url") // 프로필 이미지 URL
    private String profileImage; // 추가: 프로필 이미지

    private String provider; // 소셜 로그인 제공자 (예: "Kakao", "Google")

    private String providerId; // 소셜 로그인 제공자의 고유 ID

    @Column(unique = true)
    private String socialId; // provider + "_" + providerId

    private String refreshToken; //FIXME 임시 사용 후 삭제 예정

    public static Member from(SignUpReqDto form, String encodedPassword) {
        Role role = Role.valueOf(form.getRole().toUpperCase());
        LoginType loginType = LoginType.valueOf(form.getLoginType().toUpperCase());

        Member.MemberBuilder memberBuilder = Member.builder()
                .loginType(loginType)
                .role(role)
                .email(form.getEmail().toLowerCase(Locale.ROOT))
                .password(encodedPassword) // 암호화된 비밀번호 사용
                .name(form.getName())
                .nickname(form.getNickname())
                .phone(form.getPhone());

        // 역할이 사장님인 경우 사업자 등록번호 추가 (필수)
        if (Role.OWNER == role) {
            memberBuilder.businessNumber(form.getBusinessNumber());
        }

        return memberBuilder.build();
    }
}
