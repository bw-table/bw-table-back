package com.zero.bwtableback.member.service;

import com.zero.bwtableback.member.dto.SignupForm;
import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.member.repository.MemberRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public MemberServiceImpl(MemberRepository memberRepository, BCryptPasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Member signupMember(SignupForm form) {
        // 이메일 유효성 검사 및 중복 체크
        validateEmail(form.getEmail());

        // 닉네임 유효성 검사 및 중복 체크
        validateNickname(form.getNickname());

        // 비밀번호 유효성 검사
        validatePassword(form.getPassword());

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

    // 닉네임 중복 체크 함수
    public boolean isNicknameDuplicate(String nickname) {
        return memberRepository.existsByNickname(nickname);
    }

    // 이메일 유효성 검사
    private void validateEmail(String email) {
        // FIXME Regex 변경
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        if (email == null || email.length() < 3 || email.length() > 50 || !email.matches(emailRegex)) {
            throw new IllegalArgumentException("유효하지 않은 이메일 형식입니다.");
        }

        if (isEmailDuplicate(email)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
    }

    // 닉네임 유효성 검사
    private void validateNickname(String nickname) {
        if (nickname.length() < 2 || nickname.length() > 15 || !nickname.matches("^[a-zA-Z0-9가-힣]+$")) {
            throw new IllegalArgumentException("유효하지 않은 닉네임입니다.");
        }

        if (isNicknameDuplicate(nickname)) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }
    }

    // 비밀번호 유효성 검사
    private void validatePassword(String password) {
        if (password.length() < 8 || !password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$")) {
            throw new IllegalArgumentException("비밀번호는 최소 8자 이상이며 대문자, 소문자, 숫자 및 특수문자를 포함해야 합니다.");
        }
    }
}
