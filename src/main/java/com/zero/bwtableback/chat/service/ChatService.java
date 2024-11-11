package com.zero.bwtableback.chat.service;

import com.zero.bwtableback.chat.dto.ChatRoomCreateRequest;
import com.zero.bwtableback.chat.dto.ChatRoomCreateResponse;
import com.zero.bwtableback.chat.dto.MessageDto;
import com.zero.bwtableback.chat.entity.ChatRoom;
import com.zero.bwtableback.chat.repository.ChatRoomRepository;
import com.zero.bwtableback.restaurant.entity.Restaurant;
import com.zero.bwtableback.restaurant.repository.RestaurantRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private ChatRoomRepository chatRoomRepository;
    private RestaurantRepository restaurantRepository;

    // 예약 확정 시 자동으로 채팅방 생성
    public ChatRoomCreateResponse createChatRoom(ChatRoomCreateRequest chatRoom) {
        Restaurant restaurant = restaurantRepository.findById(chatRoom.getRestaurant().getId())
                .orElseThrow(() -> new RuntimeException("식당이 존재하지 않습니다."));

        return null;
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
