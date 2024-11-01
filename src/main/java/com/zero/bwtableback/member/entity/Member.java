package com.zero.bwtableback.member.entity;

import com.zero.bwtableback.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "member") // 데이터베이스의 테이블 이름
public class Member extends BaseEntity {

    @Id
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
}
