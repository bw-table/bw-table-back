package com.zero.bwtableback.member.service;

import com.zero.bwtableback.common.exception.CustomException;
import com.zero.bwtableback.common.exception.ErrorCode;
import com.zero.bwtableback.member.dto.EmailLoginReqDto;
import com.zero.bwtableback.member.dto.SignUpReqDto;
import com.zero.bwtableback.member.dto.SignUpResDto;
import com.zero.bwtableback.member.dto.TokenDto;
import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.member.entity.Role;
import com.zero.bwtableback.member.repository.MemberRepository;
import com.zero.bwtableback.security.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 사용자 인증 및 권한 부여를 처리하는 서비스 클래스
 *
 * 로그인, 회원가입, 토큰 갱신 등 인증 관련 비즈니스 로직 포함
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    /**
     * 이메일 중복 확인
     */
    public boolean isEmailDuplicate(String email) {
        return memberRepository.existsByEmail(email);
    }

    /**
     * 닉네임 중복 확인
     */
    public boolean isNicknameDuplicate(String nickname) {
        return memberRepository.existsByNickname(nickname);
    }

    /**
     * 전화번호 중복 확인
     */
    public boolean isPhoneDuplicate(String phone) {
        return memberRepository.existsByPhone(phone);
    }

    /**
     * 사업자등록번호 중복 확인 (사장님만)
     */
    public boolean isBusinessNumberDuplicate(String businessNumber) {
        return memberRepository.existsByBusinessNumber(businessNumber);
    }

    /**
     * 새로운 사용자 이메일 회원가입
     */
    public SignUpResDto signUp(SignUpReqDto form) {
        // 이메일 중복 체크
        if (isEmailDuplicate(form.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        // 닉네임 중복 체크
        if (isNicknameDuplicate(form.getNickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }
        // 전화번호 중복 체크 및 하이픈 제거
        if (isPhoneDuplicate(form.getPhone())) {
//            form.setPhone(cleanPhoneNumber(form.getPhone()));
            throw new IllegalArgumentException("이미 사용 중인 전화번호입니다.");
        }
        // 사업자등록번호 유효성 검사 및 하이픈 제거(사장님 회원가입 시)
        if (form.getRole() == Role.OWNER) {
//            form.setBusinessNumber(cleanBusinessNumber(form.getBusinessNumber()));

            // 사업자등록번호 중복 체크
            if (isBusinessNumberDuplicate(form.getBusinessNumber())) {
                throw new IllegalArgumentException("이미 사용 중인 사업자등록번호입니다.");
            }
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(form.getPassword());

        Member member = Member.from(form, encodedPassword);

        Member savedMember = memberRepository.save(member);

        return SignUpResDto.from(savedMember);
    }

    // FIXME util로 이동, 사용 여부 결정
    // 전화번호 하이픈 제거
    private String cleanPhoneNumber(String phone) {
        return phone.replaceAll("-", "").trim();
    }
    // 사업자등록번호 하이픈 제거
    private String cleanBusinessNumber(String businessNumber) {
        return businessNumber.replaceAll("-", "").trim();
    }

    /**
     * 사용자 로그인을 처리하고 인증 토큰을 반환
     */
    public TokenDto login(EmailLoginReqDto loginDto) {
        Member member = memberRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CREDENTIALS));

        // 비밀번호 확인
        if (!passwordEncoder.matches(loginDto.getPassword(), member.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }

        // 토큰 생성
        String accessToken = tokenProvider.createAccessToken(member.getEmail());
        String refreshToken = tokenProvider.createRefreshToken();

        // TODO 리프레시 토큰 저장 (레디스)

        // FIXME 리프레시 토큰 임시 저장 (Member DB) 저장소 변경 후 setter Member의 @Setter 삭제
        member.setRefreshToken(refreshToken);
        memberRepository.save(member);

        // TokenDto 생성 및 반환
        return new TokenDto(accessToken, refreshToken);
    }

    /**
     * 리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급
     */
    public TokenDto refreshToken(String refreshToken) {

        return null;
    }

    /**
     * 사용자 로그아웃 처리
     */
    public void logout() {
//        Member member = memberRepository.findById()
//                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    }
}