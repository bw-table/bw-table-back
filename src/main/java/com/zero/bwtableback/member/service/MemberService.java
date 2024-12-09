package com.zero.bwtableback.member.service;

import com.zero.bwtableback.chat.dto.ChatRoomCreateResDto;
import com.zero.bwtableback.chat.repository.ChatRoomRepository;
import com.zero.bwtableback.common.exception.CustomException;
import com.zero.bwtableback.common.exception.ErrorCode;
import com.zero.bwtableback.member.dto.MemberDto;
import com.zero.bwtableback.member.dto.MemberPrivateDto;
import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.member.repository.MemberRepository;
import com.zero.bwtableback.reservation.dto.ReservationResDto;
import com.zero.bwtableback.reservation.repository.ReservationRepository;
import com.zero.bwtableback.restaurant.dto.ReviewDetailDto;
import com.zero.bwtableback.restaurant.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final ReservationRepository reservationRepository;
    private final ReviewRepository reviewRepository;
    private final ChatRoomRepository chatRoomRepository;

    /**
     * 회원 목록 조회
     */
    public Page<MemberDto> getMembers(Pageable pageable) {
        return memberRepository.findAll(pageable)
                .map(MemberDto::from);
    }

    /**
     * 회원 정보 조회
     */
    public MemberDto getMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return MemberDto.from(member);
    }

    /**
     * 본인 정보 조회
     */
    public MemberPrivateDto getMyInfo(Long memberId) {
        Member member = getMemberById(memberId);
        return MemberPrivateDto.from(member);
    }

    /**
     * 나의 예약 목록 조회
     */
    public Page<ReservationResDto> getMyReservations(Pageable pageable, Long memberId) {
        Member member = getMemberById(memberId);
        return reservationRepository.findByMemberId(member.getId(), pageable)
                .map(ReservationResDto::fromEntity);
    }

    /**
     * 나의 리뷰 목록 조회
     */
    public Page<ReviewDetailDto> getMyReviews(Pageable pageable, Long memberId) {
        Member member = getMemberById(memberId);
        return reviewRepository.findByMemberIdOrderByRestaurantId(member.getId(), pageable)
                .map(ReviewDetailDto::fromEntity);
    }

    /**
     * 나의 채팅방 조회
     */
    public Page<ChatRoomCreateResDto> getMyChatRooms(Pageable pageable, Long memberId) {
        Member member = getMemberById(memberId);
        return chatRoomRepository.findChatRoomsByMemberIdOrderByLastMessageTime(member.getId(), pageable)
                .map(ChatRoomCreateResDto::fromEntity);
    }
    
    private Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}