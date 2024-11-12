package com.zero.bwtableback.member.controller;

import com.zero.bwtableback.member.dto.MemberDto;
import com.zero.bwtableback.member.service.MemberService;
import com.zero.bwtableback.reservation.dto.ReservationResponseDto;
import com.zero.bwtableback.restaurant.dto.ReviewInfoDto;
import com.zero.bwtableback.restaurant.dto.ReviewResDto;
import com.zero.bwtableback.security.MemberDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
     */
    @GetMapping("/me")
    public MemberDto getMyInfo(@AuthenticationPrincipal MemberDetails memberDetails) {
        String email = memberDetails.getUsername();
        return memberService.getMyInfo(email);
    }

    /**
     * 나의 모든 예약 조회
     */
    @GetMapping("/me/reservations")
    public ResponseEntity<Page<ReservationResponseDto>> getMyReservations(Pageable pageable,
                                                                          @AuthenticationPrincipal MemberDetails memberDetails) {
        String email = memberDetails.getUsername();

        return ResponseEntity.ok(memberService.getMyReservations(pageable,email));
    }

    /**'
     * TODO 회원 정보 수정
     */

    /**
     * TODO 회원 탈퇴
     */

    /**
     * FIXME 회원 프로필 이미지 이름 엔드포인트 변경하기
     */


    /**
     * 이메일 회원 비밀번호 변경
     */

    /**
     * 나의 모든 리뷰 조회
     */
    @GetMapping("me/reviews")
    public ResponseEntity<Page<ReviewInfoDto>> getMyReviews(Pageable pageable,
                                                            @AuthenticationPrincipal MemberDetails memberDetails) {
        String email = memberDetails.getUsername();
        return ResponseEntity.ok(memberService.getMyReviews(pageable,email));
    }
}
