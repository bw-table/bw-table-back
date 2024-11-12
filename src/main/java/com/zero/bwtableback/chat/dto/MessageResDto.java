package com.zero.bwtableback.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MessageResDto {
    private String nickname;
    private String content;
    private long timestamp;
}
