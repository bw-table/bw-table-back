package com.zero.bwtableback.chat.dto;

import com.zero.bwtableback.chat.entity.Message;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MessageDto {
    private Long id;
    private Long chatRoomId;
    private Long senderId;
    private String senderName;
    private Long restaurantId;
    private String content;
    private LocalDateTime timestamp;

    public static MessageDto from(Message message) {
        MessageDto dto = new MessageDto();
        dto.id = message.getId();
        dto.chatRoomId = message.getChatRoom().getId();
        dto.senderId = message.getSender().getId();
        dto.senderName = message.getSender().getName(); // 발신자 이름이 필요할 경우
        dto.restaurantId = message.getRestaurant().getId();
        dto.content = message.getContent();
        dto.timestamp = message.getTimestamp();
        return dto;
    }
}
