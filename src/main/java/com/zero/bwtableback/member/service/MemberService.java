package com.zero.bwtableback.member.service;

import com.zero.bwtableback.common.exception.CustomException;
import com.zero.bwtableback.common.exception.ErrorCode;
import com.zero.bwtableback.member.dto.MemberDto;
import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    /**
     * 회원 목록 조회
     */
    public Page<MemberDto> getMembers(Pageable pageable) {
        return memberRepository.findAll(pageable)
                .map(this::convertToDto);
    }

    /**
     * 단일 회원 정보 조회
     */
    public MemberDto getMemberById(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return convertToDto(member);
    }

    /**
     * 본인 정보 조회
     * FIXME 코드 리뷰 (X) : 토큰에서 검증과 함께 구현 예정
     */
    public MemberDto getMyInfo(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return convertToDto(member);
    }

    private MemberDto convertToDto(Member member) {
        return new MemberDto(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getNickname(),
                member.getPhone(),
                member.getRole());
    }
}