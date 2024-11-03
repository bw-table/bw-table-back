package com.zero.bwtableback.member.service;

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

    private MemberDto convertToDto(Member member) {
        return new MemberDto(
        member.getId(),
                member.getEmail(),
                member.getName(),
                member.getNickname(),
                member.getPhone(),
                member.getRole())
        ;
    }
}