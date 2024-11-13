package com.zero.bwtableback.chat.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MessageReqDto {
    private String content;
    private long timestamp;
}
