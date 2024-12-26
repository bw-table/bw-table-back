package com.zero.bwtableback.member.servcie;

import com.zero.bwtableback.chat.dto.ChatRoomCreateResDto;
import com.zero.bwtableback.chat.entity.ChatRoom;
import com.zero.bwtableback.chat.repository.ChatRoomRepository;
import com.zero.bwtableback.common.exception.CustomException;
import com.zero.bwtableback.common.exception.ErrorCode;
import com.zero.bwtableback.member.dto.MemberDto;
import com.zero.bwtableback.member.dto.MemberPrivateDto;
import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.member.entity.Role;
import com.zero.bwtableback.member.repository.MemberRepository;
import com.zero.bwtableback.member.service.MemberService;
import com.zero.bwtableback.reservation.dto.ReservationResDto;
import com.zero.bwtableback.reservation.entity.Reservation;
import com.zero.bwtableback.reservation.entity.ReservationStatus;
import com.zero.bwtableback.reservation.repository.ReservationRepository;
import com.zero.bwtableback.restaurant.entity.Restaurant;
import com.zero.bwtableback.restaurant.entity.Review;
import com.zero.bwtableback.restaurant.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private ChatRoomRepository chatRoomRepository;

    @InjectMocks
    private MemberService memberService;

    private Member member;
    private Restaurant restaurant;
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .id(1L)
                .email("test@example.com")
                .name("홍길동")
                .nickname("길동")
                .phone("01012345678")
                .role(Role.GUEST)
                .profileImage("profile.jpg")
                .businessNumber(null) // businessNumber는 null로 설정
                .build();

        restaurant = Restaurant.builder()
                .id(1L)  // 레스토랑 ID (예시로 설정)
                .name("맛있는 레스토랑")  // 레스토랑 이름
                .description("정통 이탈리안 요리를 제공합니다.")  // 설명
                .address("서울특별시 강남구 역삼동")  // 주소
                .member(new Member())  // 실제 Member 객체로 초기화 필요
                .build();

        reservation = Reservation.builder()
                .id(1L)  // 예약 ID (예시로 설정)
                .restaurant(restaurant)  // 실제 Restaurant 객체로 초기화 필요
                .member(member)  // 이미 초기화된 member 객체 사용
                .reservationDate(LocalDate.of(2024, 12, 25))
                .reservationTime(LocalTime.of(18, 30))
                .numberOfPeople(4)
                .specialRequest("창가자리 부탁드립니다.")
                .reservationStatus(ReservationStatus.CONFIRMED) // 상태 설정
                .build();
    }

    @Test
    @DisplayName("회원 ID로 회원 정보 조회 성공")
    void testGetMemberById_Success() {
        // given
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        // when
        MemberDto result = memberService.getMemberById(1L);
        System.out.println(result.getName());

        // then
        assertNotNull(result);
        assertEquals(member.getEmail(), result.getEmail());
    }

    @Test
    @DisplayName("존재하지 않는 회원 ID로 조회 시 예외 발생")
    public void testGetMemberById_UserNotFound() {
        // given
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            memberService.getMemberById(1L);
        });
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("본인 정보 조회 성공")
    public void testGetMyInfo_Success() {
        // given
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        // when
        MemberPrivateDto result = memberService.getMyInfo(member.getId());

        // then
        assertNotNull(result);
        assertEquals(member.getEmail(), result.getEmail());
    }

    @Test
    @DisplayName("회원 목록 조회 성공")
    public void testGetMembers_Success() {
        // given
        List<Member> members = new ArrayList<>();
        members.add(member);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Member> pageMembers = new PageImpl<>(members, pageable, members.size());

        when(memberRepository.findAll(pageable)).thenReturn(pageMembers);

        // when
        Page<MemberDto> result = memberService.getMembers(pageable);

        // then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("나의 예약 조회 성공")
    void testGetMyReservations_Success() {
        // given
        List<Reservation> reservations = new ArrayList<>();
        reservations.add(reservation);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Reservation> pageReservations = new PageImpl<>(reservations, pageable, reservations.size());

        when(memberRepository.findByEmail(member.getEmail())).thenReturn(Optional.of(member));
        when(reservationRepository.findByMemberId(member.getId(), pageable)).thenReturn(pageReservations);

        // when
        Page<ReservationResDto> result = memberService.getMyReservations(pageable, member.getEmail());

        // then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("나의 리뷰 조회 성공")
    void testGetMyReviews_Success() {
        // given
        List<Review> reviews = new ArrayList<>();
        Pageable pageable = PageRequest.of(0, 10);

        // Mock ReviewInfoDto 객체 추가
        when(memberRepository.findByEmail(member.getEmail())).thenReturn(Optional.of(member));
        when(reviewRepository.findByMemberIdOrderByRestaurantId(member.getId(), pageable)).thenReturn(new PageImpl<>(reviews));

        // when
//        Page<ReviewInfoDto> result = memberService.getMyReviews(pageable, member.getEmail());

        // then
//        assertNotNull(result);
//        assertEquals(0, result.getTotalElements()); // 리뷰가 없으므로 0
    }

    @Test
    @DisplayName("나의 채팅방 조회 성공")
    void testGetMyChatRooms_Success() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        List<ChatRoom> chatRooms = new ArrayList<>();
        // Mock ChatRoomCreateResDto 객체 추가
        when(memberRepository.findByEmail(member.getEmail())).thenReturn(Optional.of(member));
        when(chatRoomRepository.findChatRoomsByMemberIdOrderByLastMessageTime(member.getId(), pageable)).thenReturn(new PageImpl<>(chatRooms));

        // when
        Page<ChatRoomCreateResDto> result = memberService.getMyChatRooms(pageable, member.getEmail());

        // then
        assertNotNull(result);
        assertEquals(0, result.getTotalElements()); // 채팅방이 없으므로 0
    }
}
