package com.zero.bwtableback.chat.repository;

import com.zero.bwtableback.chat.entity.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Page<ChatRoom> findByRestaurant_Id(Long restaurantId, Pageable pageable);
}