package com.zero.bwtableback.member.repository;

import com.zero.bwtableback.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    // 이메일로 회원 찾기
    Optional<Member> findByEmail(String email);

    // 소셜 ID로 회원 찾기
    Optional<Member> findBySocialId(String socialId);

    // 회원가입 시 유효성 검사
    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);
    boolean existsByPhone(String phone);
    boolean existsByBusinessNumber(String businessNumber);

}
