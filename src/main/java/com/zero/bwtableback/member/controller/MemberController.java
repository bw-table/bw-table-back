package com.zero.bwtableback.member.controller;

import com.zero.bwtableback.member.dto.MemberDto;
import com.zero.bwtableback.member.service.MemberService;
import com.zero.bwtableback.security.MemberDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    /**
     * 회원 목록 조회
     */
    @GetMapping
    public ResponseEntity<Page<MemberDto>> getMembers(Pageable pageable) {
        return ResponseEntity.ok(memberService.getMembers(pageable));
    }

    /**
     * 특정 회원 정보 조회
     */
    @GetMapping("/{memberId}")
    public MemberDto getMemberById(@PathVariable Long memberId) {
        return memberService.getMemberById(memberId);
    }

    /**
     * 본인 정보 조회
     *
     * JWT 토큰으로 현재 사용자 인증
     *
     *  FIXME 코드 리뷰 (X) : 토큰에서 검증과 함께 구현 예정
     */
    @GetMapping("/me")
    public MemberDto getMyInfo(@AuthenticationPrincipal MemberDetails memberDetails) {
        String email = memberDetails.getUsername();
        return memberService.getMyInfo(email);
    }
}
