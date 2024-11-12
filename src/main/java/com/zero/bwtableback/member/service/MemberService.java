package com.zero.bwtableback.member.service;

import com.zero.bwtableback.common.exception.CustomException;
import com.zero.bwtableback.common.exception.ErrorCode;
import com.zero.bwtableback.member.dto.MemberDto;
import com.zero.bwtableback.member.dto.MemberPrivateDto;
import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.member.repository.MemberRepository;
import com.zero.bwtableback.reservation.dto.ReservationResponseDto;
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
     */
    public MemberPrivateDto getMyInfo(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return convertToPrivateDto(member);
    }

    private MemberDto convertToDto(Member member) {
        return new MemberDto(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getNickname(),
                member.getPhone(),
                member.getRole(),
                member.getProfileImage(),
                member.getBusinessNumber());
    }

    private MemberPrivateDto convertToPrivateDto(Member member) {
        return new MemberPrivateDto(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getNickname(),
                member.getPhone(),
                member.getRole(),
                member.getProfileImage(),
                member.getBusinessNumber(),
                member.getLoginType());
    }

//    public Page<ReservationResponseDto> getMyReservations(Pageable pageable) {
//        Long userId = ...; // 현재 사용자 ID 가져오기
//
//        // 사용자의 모든 예약을 조회
//        return reservationRepository.findByUserId(userId, pageable)
//                .map(reservation -> new ReservationResponseDto(reservation)); // DTO로 변환
//    }
//
//    public Page<ReviewResDto> getMyReviews(Pageable pageable) {
//        // 현재 로그인한 사용자 ID를 가져오는 방법 (예: SecurityContextHolder 사용)
//        Long userId = ...; // 현재 사용자 ID 가져오기
//
//        // 사용자의 모든 리뷰를 조회
//        return reviewRepository.findByUserId(userId, pageable)
//                .map(review -> new ReviewResDto(review)); // DTO로 변환
//    }
}