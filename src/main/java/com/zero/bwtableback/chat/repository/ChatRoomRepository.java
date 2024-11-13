package com.zero.bwtableback.chat.repository;

import com.zero.bwtableback.chat.entity.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Page<ChatRoom> findByRestaurantId(Long restaurantId, Pageable pageable);

    // 채팅방의 마지막 메시지로 정렬
    @Query("SELECT cr FROM ChatRoom cr JOIN cr.messages m " +
            "WHERE cr.member.id = :memberId " +
            "ORDER BY m.timestamp DESC")

    Page<ChatRoom> findChatRoomsByMemberIdOrderByLastMessageTime(@Param("memberId") Long memberId, Pageable pageable);
}