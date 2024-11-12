package com.zero.bwtableback.chat.entity;

import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.restaurant.entity.Restaurant;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "message")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private Member sender;

    private String content;

    private LocalDateTime timestamp;

    @ManyToOne
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Builder
    public Message(String content, Member sender, ChatRoom chatRoom, Restaurant restaurant) {
        this.content = content;
        this.sender = sender;
        this.chatRoom = chatRoom;
        this.restaurant = restaurant;
        this.timestamp = LocalDateTime.now();
    }
}
