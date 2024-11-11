package com.zero.bwtableback.chat.service;

import com.zero.bwtableback.chat.dto.ChatRoomCreateResDto;
import com.zero.bwtableback.chat.dto.MessageDto;
import com.zero.bwtableback.chat.entity.ChatRoom;
import com.zero.bwtableback.chat.entity.ChatRoomStatus;
import com.zero.bwtableback.chat.repository.ChatRoomRepository;
import com.zero.bwtableback.common.exception.CustomException;
import com.zero.bwtableback.common.exception.ErrorCode;
import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.member.repository.MemberRepository;
import com.zero.bwtableback.reservation.dto.ReservationResponseDto;
import com.zero.bwtableback.reservation.entity.Reservation;
import com.zero.bwtableback.reservation.repository.ReservationRepository;
import com.zero.bwtableback.restaurant.entity.Restaurant;
import com.zero.bwtableback.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;
    private final RestaurantRepository restaurantRepository;
    private final ReservationRepository reservationRepository;

    /**
     * 예약 확정 시 자동으로 채팅방 생성
     *
     * @return 예약 정보, 가게 정보
     */
    public ChatRoomCreateResDto createChatRoom(ReservationResponseDto reservationResDto) {
        Restaurant restaurant = restaurantRepository.findById(reservationResDto.restaurantId())
                .orElseThrow(() -> new RuntimeException("식당이 존재하지 않습니다."));

        String restaurantName = restaurant.getName();
        LocalDate reservationDate = reservationResDto.reservationDate();
        LocalTime reservationTime = reservationResDto.reservationTime();

        String formattedTime = reservationTime.format(DateTimeFormatter.ofPattern("HH:mm"));

        // 채팅방 이름 생성
        String roomName = String.format("%s - %s %s", restaurantName, reservationDate, formattedTime);

        // 새로운 채팅방 객체 생성
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setRoomName(roomName);
        chatRoom.setStatus(ChatRoomStatus.ACTIVE);
        chatRoom.setRestaurant(restaurant);

        Reservation reservation = reservationRepository.findById(reservationResDto.reservationId())
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));
        chatRoom.setReservation(reservation);

        Member member = memberRepository.findById(reservationResDto.memberId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        chatRoom.setMember(member);

        chatRoomRepository.save(chatRoom);

        return new ChatRoomCreateResDto(
                chatRoom.getId(),
                chatRoom.getRoomName(),
                chatRoom.getStatus(),
                chatRoom.getReservation(),
                chatRoom.getMember(),
                chatRoom.getRestaurant()
        );
    }

    // 특정 회원의 모든 채팅방 조회
    public Page<ChatRoom> getChatRoomsByMemberId(Long memberId, Pageable pageable) {
        return null;
    }

    // 특정 식당의 모든 채팅방 조회
    public Page<ChatRoom> getAllChatRoomsByRestaurantId(Long restaurantId, Pageable pageable) {
        return chatRoomRepository.findByRestaurant_Id(restaurantId, pageable);
    }

    // 특정 채팅방 조회
    public ChatRoom getChatRoomById(Long chatRoomId) {
        return chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));
    }

    // FIXME 특정 채팅방 삭제 (비활성으로 처리 예정)
    public void deleteChatRoom(Long chatRoomId) {
        chatRoomRepository.deleteById(chatRoomId); // 채팅방 삭제
    }

    // 특정 채팅방 비활성화
    public void deactivateChatRoom(Long chatRoomId) {
    }

    // 특정 채팅방 메시지 조회
    public Page<MessageDto> getMessages(Long chatRoomId, Pageable pageable) {

        return null;
    }

    // 메시지 전송
    public MessageDto sendMessage(Long chatRoomId, MessageDto messageDto) {
        return messageDto;
    }
}
