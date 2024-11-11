package com.zero.bwtableback.chat.dto;

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
}
