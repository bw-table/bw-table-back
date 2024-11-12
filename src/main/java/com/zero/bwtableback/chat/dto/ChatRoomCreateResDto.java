package com.zero.bwtableback.chat.dto;

import com.zero.bwtableback.chat.entity.ChatRoom;
import com.zero.bwtableback.chat.entity.ChatRoomStatus;
import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.reservation.entity.Reservation;
import com.zero.bwtableback.restaurant.entity.Restaurant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatRoomCreateResDto {
    private Long id;
    private String roomName;
    private ChatRoomStatus status;
    private Long reservationId;
    private Long memberId;
    private Long restaurantId;

    public static ChatRoomCreateResDto fromEntity(ChatRoom chatRoom) {
        return ChatRoomCreateResDto.builder()
                .id(chatRoom.getId())
                .roomName(chatRoom.getRoomName())
                .status(chatRoom.getStatus())
                .reservationId(chatRoom.getReservation().getId())
                .memberId(chatRoom.getMember().getId())
                .restaurantId(chatRoom.getRestaurant().getId())
                .build();
    }
}
