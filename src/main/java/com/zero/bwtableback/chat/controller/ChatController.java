package com.zero.bwtableback.chat.controller;

import com.zero.bwtableback.chat.dto.MessageDto;
import com.zero.bwtableback.chat.entity.ChatRoom;
import com.zero.bwtableback.chat.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chats")
public class ChatController {
    private final ChatService chatService;

    // 채팅방 생성 엔드포인트는 예약 확정 시 자동으로 생성

    // 특정 회원의 모든 채팅방 조회
    @GetMapping("/user/{userId}/rooms")
    public ResponseEntity<Page<ChatRoom>> getUserChatRooms(@PathVariable Long userId, Pageable pageable) {
        Page<ChatRoom> chatRooms = chatService.getChatRoomsByMemberId(userId, pageable);
        return ResponseEntity.ok(chatRooms);
    }

    // 특정 식당의 모든 채팅방 조회
    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<Page<ChatRoom>> getAllChatRooms(@PathVariable Long restaurantId, Pageable pageable) {
        Page<ChatRoom> chatRooms = chatService.getAllChatRoomsByRestaurantId(restaurantId, pageable);
        return ResponseEntity.ok(chatRooms);
    }

    // FIXME 특정 채팅방 조회 (필요한가?)
    @GetMapping("/{chatRoomId}")
    public ResponseEntity<ChatRoom> getChatRoomById(@PathVariable Long chatRoomId) {
        ChatRoom chatRoom = chatService.getChatRoomById(chatRoomId);
        return ResponseEntity.ok(chatRoom);
    }

    // FIXME 특정 채팅방 비활성화 (엔드포인트가 필요한가?)
    @PatchMapping("/{chatRoomId}/deactivate")
    public ResponseEntity<Void> deactivateChatRoom(@PathVariable Long chatRoomId) {
        chatService.deactivateChatRoom(chatRoomId);
        return ResponseEntity.noContent().build();
    }

    // FIXME 특정 채팅방 삭제 (비활성으로 처리 예정)
    @DeleteMapping("/{chatRoomId}")
    public ResponseEntity<Void> deleteChatRoom(@PathVariable Long chatRoomId) {
        chatService.deleteChatRoom(chatRoomId);
        return ResponseEntity.noContent().build();
    }

    // 특정 채팅방의 전체 메시지 조회
    @GetMapping("/{chatRoomId}/messages")
    public ResponseEntity<Page<MessageDto>> getMessages(@PathVariable Long chatRoomId,
                                                        @RequestParam(defaultValue = "0") int page, // TODO 기본값 변경
                                                        @RequestParam(defaultValue = "20") int size,
                                                        @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "timestamp"));
        Page<MessageDto> messages = chatService.getMessages(chatRoomId, pageable);
        return ResponseEntity.ok(messages);
    }

    // 메시지 전송
    @PostMapping("/{chatRommId}/message")
    public ResponseEntity<MessageDto> sendMessage(@PathVariable Long chatRoomId, @Valid MessageDto messageDto) {
        MessageDto message = chatService.sendMessage(chatRoomId, messageDto);
        return ResponseEntity.ok(message);
    }
}