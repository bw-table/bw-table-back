package com.zero.bwtableback.chat.controller;

import com.zero.bwtableback.chat.entity.ChatRoom;
import com.zero.bwtableback.chat.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chatrooms")
public class ChatRoomController {
    private final ChatRoomService chatRoomService;

    // 식당의 전체 채팅방 생성
    @PostMapping
    public ResponseEntity<ChatRoom> createChatRoom(@PathVariable Long restaurantId, @RequestBody ChatRoom chatRoom) {
        ChatRoom createdChatRoom = chatRoomService.createChatRoom(restaurantId, chatRoom);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdChatRoom);
    }

    // 특정 식당의 모든 채팅방 조회
    @GetMapping
    public ResponseEntity<Page<ChatRoom>> getAllChatRooms(@PathVariable Long restaurantId, Pageable pageable) {
        Page<ChatRoom> chatRooms = chatRoomService.getAllChatRoomsByRestaurantId(restaurantId, pageable);
        return ResponseEntity.ok(chatRooms);
    }

    // 특정 식당의 특정 채팅방 조회
    @GetMapping("/{id}")
    public ResponseEntity<ChatRoom> getChatRoomById(@PathVariable Long restaurantId, @PathVariable Long id) {
        ChatRoom chatRoom = chatRoomService.getChatRoomById(id);
        return ResponseEntity.ok(chatRoom);
    }

    // 특정 채팅방 비활성화
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateChatRoom(@PathVariable Long restaurantId, @PathVariable Long id) {
        chatRoomService.deactivateChatRoom(id);
        return ResponseEntity.noContent().build();
    }

    // FIXME 특정 채팅방 삭제 (필요한 않는 경우 삭제)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChatRoom(@PathVariable Long restaurantId, @PathVariable Long id) {
        chatRoomService.deleteChatRoom(id);
        return ResponseEntity.noContent().build();
    }
}
