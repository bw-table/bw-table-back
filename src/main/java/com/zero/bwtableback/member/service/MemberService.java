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
import com.zero.bwtableback.restaurant.dto.ReviewInfoDto;
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

    public Page<ReservationResDto> getMyReservations(Pageable pageable, String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return reservationRepository.findByMemberId(member.getId(), pageable)
                .map(ReservationResDto::fromEntity);
    }

    public Page<ReviewInfoDto> getMyReviews(Pageable pageable, String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return reviewRepository.findByMemberIdOrderByRestaurantId(member.getId(), pageable)
                .map(ReviewInfoDto::fromEntity);
    }

    public Page<ChatRoomCreateResDto> getMyChatRooms(Pageable pageable, String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return chatRoomRepository.findChatRoomsByMemberIdOrderByLastMessageTime(member.getId(), pageable)
                .map(ChatRoomCreateResDto::fromEntity);
    }
}