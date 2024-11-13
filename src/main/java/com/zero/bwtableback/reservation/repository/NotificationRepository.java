package com.zero.bwtableback.reservation.repository;

import com.zero.bwtableback.reservation.entity.Notification;
import com.zero.bwtableback.reservation.entity.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 고객 회원이 받은 알림 조회
    Page<Notification> findByReservation_Member_IdAndStatusOrderByScheduledTimeDesc(
            Long memberId, NotificationStatus status, Pageable pageable);

    // 가게 주인이 받은 알림 조회
    // TODO: 가게 엔티티에 회원 필드가 추가되면 활성화
//    Page<Notification> findByReservation_Restaurant_Member_IdAndStatusOrderByScheduledTimeDesc(
//            Long memberId, NotificationStatus status, Pageable pageable);

}
