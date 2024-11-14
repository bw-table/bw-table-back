package com.zero.bwtableback.reservation.repository;

import com.zero.bwtableback.reservation.entity.Notification;
import com.zero.bwtableback.reservation.entity.NotificationStatus;
import io.lettuce.core.dynamic.annotation.Param;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 고객 회원이 받은 알림 조회
    Page<Notification> findByReservation_Member_IdAndStatusOrderByScheduledTimeDesc(
            Long memberId, NotificationStatus status, Pageable pageable);

    // 가게 주인이 받은 알림 조회
    Page<Notification> findByReservation_Restaurant_Member_IdAndStatusOrderByScheduledTimeDesc(
            Long memberId, NotificationStatus status, Pageable pageable);

    // cutoffDate(기준일) 지난 알림의 ID만 조회
    @Query("SELECT n.id FROM Notification n WHERE n.sentTime < :cutoffDate AND n.status = :status")
    Page<Long> findIdsBySentTimeBeforeAndStatus(@Param("cutoffDate") LocalDateTime cutoffDate,
                                                @Param("status") NotificationStatus status,
                                                Pageable pageable);

}
