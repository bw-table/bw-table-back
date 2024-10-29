package com.zero.bwtableback.member.service;

import com.zero.bwtableback.member.dto.SignupForm;
import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public MemberServiceImpl(MemberRepository memberRepository, BCryptPasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public Member registerMember(SignupForm form) {
        // 이메일 중복 체크
        if (isEmailDuplicate(form.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(form.getPassword());
        form.setPassword(encodedPassword);

        // Member 객체 생성 및 저장
        Member member = Member.from(form);
        return memberRepository.save(member);
    }

    // 이메일 중복 체크 함수
    public boolean isEmailDuplicate(String email) {
        return memberRepository.existsByEmail(email);
    }
}
