package com.zero.bwtableback.chat.controller;

import com.zero.bwtableback.chat.dto.MessageReqDto;
import com.zero.bwtableback.chat.dto.MessageResDto;
import com.zero.bwtableback.chat.entity.ChatRoom;
import com.zero.bwtableback.chat.service.ChatService;
import com.zero.bwtableback.common.exception.CustomException;
import com.zero.bwtableback.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chats")
public class ChatController {
    private final ChatService chatService;

    // 채팅방 생성 엔드포인트는 예약 확정 시 자동으로 생성
    
    // FIXME 특정 채팅방 조회 (필요 여부 판단)
    @GetMapping("/{chatRoomId}")
    public ResponseEntity<ChatRoom> getChatRoomById(@PathVariable Long chatRoomId) {
        ChatRoom chatRoom = chatService.getChatRoomById(chatRoomId);
        return ResponseEntity.ok(chatRoom);
    }

    /**
     * 특정 채팅방의 전체 메시지 조회
     */
    @GetMapping("/{chatRoomId}/messages")
    public ResponseEntity<Page<MessageResDto>> getMessages(@PathVariable Long chatRoomId,
                                                           @RequestParam(defaultValue = "0") int page, // TODO 기본값 변경
                                                           @RequestParam(defaultValue = "20") int size,
                                                           @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "timestamp"));
        Page<MessageResDto> messages = chatService.getMessages(chatRoomId, pageable);
        return ResponseEntity.ok(messages);
    }

    /**
     * 메시지 전송
     *
     * TODO Redis에 캐싱 및 배치 작업 고려
     * TODO 첫 연결 시 메시지
     * 임시로 DB에 저장
     */
    @MessageMapping("/send/{chatRoomId}")
    @SendTo("/topic/chatrooms/{chatRoomId}") // 해당 채팅방의 구독자에게 메시지 전송
    public MessageResDto send(@DestinationVariable Long chatRoomId, @Payload MessageReqDto messageReqDto, Principal principal) {
        if (!chatService.isChatRoomActive(chatRoomId)) {
            throw new CustomException(ErrorCode.CHAT_ROOM_INACTIVE);
        }
        String email = principal.getName();

        MessageResDto messageResDto = chatService.saveMessage(chatRoomId, email, messageReqDto);

        return messageResDto;
    }
}