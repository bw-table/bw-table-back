package com.zero.bwtableback.chat.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageDto {
    private Long id;
    private Long chatRoomId;
    private String sender;
    private String content;
    private LocalDateTime timestamp;
}
