package com.zero.bwtableback.chat.service;

import com.zero.bwtableback.chat.dto.MessageReqDto;
import com.zero.bwtableback.chat.dto.MessageResDto;
import com.zero.bwtableback.chat.entity.ChatRoom;
import com.zero.bwtableback.chat.entity.ChatRoomStatus;
import com.zero.bwtableback.chat.entity.Message;
import com.zero.bwtableback.chat.repository.ChatRoomRepository;
import com.zero.bwtableback.chat.repository.MessageRepository;
import com.zero.bwtableback.common.exception.CustomException;
import com.zero.bwtableback.common.exception.ErrorCode;
import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.member.repository.MemberRepository;
import com.zero.bwtableback.reservation.dto.PaymentCompleteResDto;
import com.zero.bwtableback.reservation.dto.ReservationResDto;
import com.zero.bwtableback.reservation.entity.Reservation;
import com.zero.bwtableback.reservation.repository.ReservationRepository;
import com.zero.bwtableback.restaurant.dto.RestaurantDetailDto;
import com.zero.bwtableback.restaurant.entity.Restaurant;
import com.zero.bwtableback.restaurant.repository.RestaurantRepository;
import com.zero.bwtableback.restaurant.service.RestaurantService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;
    private final MemberRepository memberRepository;
    private final RestaurantRepository restaurantRepository;
    private final ReservationRepository reservationRepository;
    private final RestaurantService restaurantService;

    /**
     * 예약 확정 시 자동으로 채팅방 생성
     *
     * @return 예약 정보, 가게 정보
     */
    public PaymentCompleteResDto createChatRoom(ReservationResDto reservationResDto) {
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

        chatRoomRepository.save(chatRoom);

        RestaurantDetailDto restaurantDetailDto = restaurantService.getRestaurantById(restaurant.getId());
        return PaymentCompleteResDto.fromEntities(restaurantDetailDto, reservation);
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
     * 특정 채팅방 조회
     */
    public ChatRoom getChatRoomById(Long chatRoomId) {
        return chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));
    }

    /**
     * 특정 채팅방 비활성화
     */
    public void inactivateChatRoom(Long chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        chatRoom.setStatus(ChatRoomStatus.INACTIVE);

        chatRoomRepository.save(chatRoom);
    }

    /**
     * 특정 채팅방 전체 메시지 조회
     */
    public Page<MessageResDto> getMessages(Long chatRoomId, Pageable pageable) {
        chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        return messageRepository.findByChatRoomIdOrderByTimestampDesc(chatRoomId, pageable)
                .map(MessageResDto::fromEntity);
    }

    /**
     * 특정 채팅방 메시지 전송
     */
    public MessageResDto saveMessage(Long chatRoomId, String email, MessageReqDto messageReqDto) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Message message = Message.builder()
                .content(messageReqDto.getContent())
                .sender(member)
                .chatRoom(chatRoom)
                .restaurant(chatRoom.getRestaurant())
                .timestamp(messageReqDto.getTimestamp())
                .build();

        messageRepository.save(message);

        return new MessageResDto(member.getNickname(), messageReqDto.getContent(), messageReqDto.getTimestamp());
    }

    public boolean isChatRoomActive(Long chatRoomId) {
        ChatRoom chatRoom = getChatRoomById(chatRoomId);
        return ChatRoomStatus.ACTIVE.equals(chatRoom.getStatus());
    }
}
