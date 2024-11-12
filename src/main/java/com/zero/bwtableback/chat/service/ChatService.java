package com.zero.bwtableback.chat.service;

import com.zero.bwtableback.chat.dto.ChatRoomCreateResDto;
import com.zero.bwtableback.chat.dto.MessageDto;
import com.zero.bwtableback.chat.entity.ChatRoom;
import com.zero.bwtableback.chat.entity.ChatRoomStatus;
import com.zero.bwtableback.chat.entity.Message;
import com.zero.bwtableback.chat.repository.ChatRoomRepository;
import com.zero.bwtableback.chat.repository.MessageRepository;
import com.zero.bwtableback.common.exception.CustomException;
import com.zero.bwtableback.common.exception.ErrorCode;
import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.member.repository.MemberRepository;
import com.zero.bwtableback.reservation.dto.PaymentCompleteDto;
import com.zero.bwtableback.reservation.dto.ReservationResponseDto;
import com.zero.bwtableback.reservation.entity.Reservation;
import com.zero.bwtableback.reservation.repository.ReservationRepository;
import com.zero.bwtableback.restaurant.entity.Restaurant;
import com.zero.bwtableback.restaurant.repository.RestaurantRepository;
import com.zero.bwtableback.security.MemberDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;
    private final MemberRepository memberRepository;
    private final RestaurantRepository restaurantRepository;
    private final ReservationRepository reservationRepository;

    /**
     * 예약 확정 시 자동으로 채팅방 생성
     *
     * @return 예약 정보, 가게 정보
     */
    public PaymentCompleteDto createChatRoom(ReservationResponseDto reservationResDto) {
        // 식당 및 예약 정보 조회
        Restaurant restaurant = getRestaurant(reservationResDto.restaurantId());
        Reservation reservation = getReservation(reservationResDto.reservationId());
        Member member = getMember(reservationResDto.memberId());

        // 채팅방 이름 생성
        String roomName = generateRoomName(restaurant.getName(), reservationResDto.reservationDate(), reservationResDto.reservationTime());

        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setRoomName(roomName);
        chatRoom.setStatus(ChatRoomStatus.ACTIVE);
        chatRoom.setRestaurant(restaurant);
        chatRoom.setReservation(reservation);
        chatRoom.setMember(member);

        // TODO 예약은 하나에 채팅방 하나
        chatRoomRepository.save(chatRoom);

        return PaymentCompleteDto.fromEntities(restaurant, reservation);
    }

    // 식당 조회
    private Restaurant getRestaurant(Long restaurantId) {
        return restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESTAURANT_NOT_FOUND));
    }

    // 예약 조회
    private Reservation getReservation(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));
    }

    // 회원 조회
    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    // 채팅방 이름 생성
    private String generateRoomName(String restaurantName, LocalDate reservationDate, LocalTime reservationTime) {
        String formattedTime = reservationTime.format(DateTimeFormatter.ofPattern("HH:mm"));
        return String.format("%s - %s %s", restaurantName, reservationDate, formattedTime);
    }

    /**
     * 특정 회원의 모든 채팅방 조회
      */
    public Page<ChatRoom> getChatRoomsByMemberId(Long memberId, Pageable pageable) {
        return null;
    }

    /**
     * 특정 식당의 모든 채팅방 조회
     */
    public Page<ChatRoom> getAllChatRoomsByRestaurantId(Long restaurantId, Pageable pageable) {
        return chatRoomRepository.findByRestaurant_Id(restaurantId, pageable);
    }

    /**
     * 특정 채팅방 조회
     */
    public ChatRoom getChatRoomById(Long chatRoomId) {
        return chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));
    }

    // FIXME 특정 채팅방 삭제 (비활성으로 처리 예정)
    public void deleteChatRoom(Long chatRoomId) {
        chatRoomRepository.deleteById(chatRoomId); // 채팅방 삭제
    }

    /**
     * 특정 채팅방 비활성화
     */
    public void deactivateChatRoom(Long chatRoomId) {
    }

    /**
     * 특정 채팅방 전체 메시지 조회
     */
    public Page<MessageDto> getMessages(Long chatRoomId, Pageable pageable) {

        return null;
    }

    /**
     * 특정 채팅방 메시지 전송
     */
    public MessageDto saveMessage(Long chatRoomId, MessageDto messageDto) {
        // TODO ACTIVE인지 확인
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        Member member = memberRepository.findById(messageDto.getSenderId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Message message = Message.builder()
                .content(messageDto.getContent())
                .sender(member)
                .chatRoom(chatRoom)
                .restaurant(chatRoom.getRestaurant())
                .build();

        messageRepository.save(message);

        return MessageDto.builder()
                .senderId(member.getId())
                .content(message.getContent())
                .timestamp(messageDto.getTimestamp())
                .build();
    }
}
