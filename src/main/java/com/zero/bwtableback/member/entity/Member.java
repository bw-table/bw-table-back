package com.zero.bwtableback.member.entity;

import com.zero.bwtableback.common.BaseEntity;
import com.zero.bwtableback.member.dto.SignUpDto;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.AuditOverride;

import java.util.Locale;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "member") // 데이터베이스의 테이블 이름
@AuditOverride(forClass = BaseEntity.class) // 엔티티가 상속받은 부모 클래스(BaseEntity)의 감사 설정
public class Member extends BaseEntity {

    @Id
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 자동 증가 PK 설정
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
    private String nickname; // 추가: 닉네임

    @Column(nullable = false)
    private String contactNumber; // 추가: 연락처

    @Enumerated(EnumType.STRING) // 역할을 문자열로 저장
    @Column(nullable = false)
    private Role role; // 추가: 역할 (손님, 사장님)

    @Column(name = "business_registration_number")
    private String businessRegistrationNumber; // 사업자 등록번호

    @Column(name = "profile_image_url") // 프로필 이미지 URL
    private String profileImage; // 추가: 프로필 이미지

    private String provider; // 소셜 로그인 제공자 (예: "Kakao", "Google")

    private String providerId; // 소셜 로그인 제공자의 고유 ID

    @Column(unique = true)
    private String socialId; // provider + "_" + providerId

    private String refreshToken; //FIXME 임시 사용 후 삭제 예정

    public static Member from(SignUpDto form) {
        Member.MemberBuilder memberBuilder = Member.builder()
                .email(form.getEmail().toLowerCase(Locale.ROOT))
                .loginType(LoginType.EMAIL)
                .password(form.getPassword())
                .name(form.getName())
                .nickname(form.getNickname())
                .role(form.getRole())
                .contactNumber(form.getContactNumber());

        // 역할이 사장님인 경우 사업자 등록번호 추가
        if (Role.OWNER == form.getRole()) {
            memberBuilder.businessRegistrationNumber(form.getBusinessNumber());
        }

        return memberBuilder.build();
    }
}
