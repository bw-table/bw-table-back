package com.zero.bwtableback.chat.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MessageReqDto {
    private String content;
    private long timestamp;
}
