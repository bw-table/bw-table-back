package com.zero.bwtableback.chat.dto;

import com.zero.bwtableback.chat.entity.ChatRoomStatus;
import com.zero.bwtableback.chat.entity.Message;
import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.reservation.entity.Reservation;
import com.zero.bwtableback.restaurant.entity.Restaurant;
import lombok.Getter;

import java.util.List;

@Getter
public class ChatRoomCreateResponse {
    private Long id;
    private String roomName;
    private ChatRoomStatus status;
    private Reservation reservation;
    private Member member;
    private Restaurant restaurant;
    private List<Message> messages;
}
