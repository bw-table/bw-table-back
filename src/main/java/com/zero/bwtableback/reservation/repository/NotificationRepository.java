package com.zero.bwtableback.reservation.repository;

import com.zero.bwtableback.reservation.entity.Notification;
import com.zero.bwtableback.reservation.entity.NotificationStatus;
import com.zero.bwtableback.reservation.entity.NotificationType;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    // 특정 알림 id보다 더 큰 값의 id를 가진 특정 유형 목록에 해당하는 알림 목록 조회
    List<Notification> findByReservation_Member_IdAndIdGreaterThanAndNotificationTypeIn(
            Long memberId, Long notificationId, List<NotificationType> types);

}
