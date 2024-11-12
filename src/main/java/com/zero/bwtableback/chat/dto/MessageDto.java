package com.zero.bwtableback.chat.dto;

import com.zero.bwtableback.chat.entity.Message;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MessageDto {
    private Long senderId;
    private String content;
    private long timestamp;
}
