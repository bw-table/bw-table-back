package com.zero.bwtableback.chat.dto;

import com.zero.bwtableback.chat.entity.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class MessageResDto {
    private String nickname;
    private String content;
    private long timestamp;

    public static MessageResDto fromEntity(Message message){
        return MessageResDto.builder()
                .nickname(message.getSender().getNickname())
                .content(message.getContent())
                .timestamp(message.getTimestamp())
                .build();
    }
}
