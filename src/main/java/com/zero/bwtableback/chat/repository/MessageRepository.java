package com.zero.bwtableback.chat.repository;

import com.zero.bwtableback.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<ChatRoom, Long> {
}
