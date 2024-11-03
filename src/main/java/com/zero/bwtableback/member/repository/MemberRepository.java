package com.zero.bwtableback.member.repository;

import com.zero.bwtableback.member.entity.Member;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);
    Optional<Member> findBySocialId(String socialId);

    // 회원가입 시 유효성 검사
    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);
    boolean existsByPhone(String phone);
    boolean existsByBusinessNumber(String businessNumber);
}
