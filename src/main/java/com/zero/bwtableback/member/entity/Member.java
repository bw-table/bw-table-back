package com.zero.bwtableback.member.entity;

import com.zero.bwtableback.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

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

    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String nickname; // 추가: 닉네임

    @Enumerated(EnumType.STRING) // 역할을 문자열로 저장
    @Column(nullable = false)
    private Role role; // 추가: 역할 (손님, 사장님)

    @Column(name = "profile_image_url") // 프로필 이미지 URL
    private String profileImage; // 추가: 프로필 이미지

    private LocalDate birthday; // 추가: 생일

    @Column(nullable = false) // 연락처는 null이 아니어야 함
    private String contactNumber; // 추가: 연락처

    @Column(nullable = false)
    private String provider; // 소셜 로그인 제공자 (예: "Kakao", "Google")

    @Column(nullable = false, unique = true)
    private String providerId; // 소셜 로그인 제공자의 고유 ID
}
