package com.zero.bwtableback.chat.service;

import com.zero.bwtableback.chat.entity.ChatRoom;
import com.zero.bwtableback.chat.repository.ChatRoomRepository;
import com.zero.bwtableback.restaurant.entity.Restaurant;
import com.zero.bwtableback.restaurant.repository.RestaurantRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ChatRoomService {

    private ChatRoomRepository chatRoomRepository;
    private RestaurantRepository restaurantRepository;

    // 채팅방 생성
    public ChatRoom createChatRoom(Long restaurantId, ChatRoom chatRoom) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("식당이 존재하지 않습니다."));

        return null;
    }

    // 특정 식당의 모든 채팅방 조회
    public Page<ChatRoom> getAllChatRoomsByRestaurantId(Long restaurantId, Pageable pageable) {
        return chatRoomRepository.findByRestaurant_Id(restaurantId, pageable);
    }

    // 특정 채팅방 조회
    public ChatRoom getChatRoomById(Long id) {
        return chatRoomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));
    }

    // 특정 채팅방 삭제
    public void deleteChatRoom(Long id) {
        chatRoomRepository.deleteById(id); // 채팅방 삭제
    }

    // 특정 채팅방 비활성화
    public void deactivateChatRoom(Long id) {
    }
}
