package com.zero.bwtableback.reservation.service;

import static com.zero.bwtableback.reservation.entity.NotificationStatus.SENT;

import com.zero.bwtableback.reservation.entity.Notification;
import com.zero.bwtableback.reservation.entity.NotificationStatus;
import com.zero.bwtableback.reservation.entity.NotificationType;
import com.zero.bwtableback.reservation.entity.Reservation;
import com.zero.bwtableback.reservation.repository.NotificationRepository;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class NotificationSearchService {

    private final NotificationRepository notificationRepository;

    // 고객 회원에게 전송된 알림 목록 조회
    public Page<Notification> getNotificationsSentToCustomer(Long customerId, Pageable pageable) {
        return notificationRepository.findByReservation_Member_IdAndStatusOrderByScheduledTimeDesc(
                customerId, SENT, pageable);
    }

    // 가게 주인 회원에게 전송된 알림 목록 조회
    public Page<Notification> getNotificationsSentToOwner(Long ownerId, Pageable pageable) {
        return notificationRepository.findByReservation_Restaurant_Member_IdAndStatusOrderByScheduledTimeDesc(
                ownerId, NotificationStatus.SENT, pageable);
    }

    // 클라이언트에서 직접 메시지를 작성을 원하는 경우 활용할 수 있는 데이터 반환
    public Map<String, Object> createNotificationData(Reservation reservation, NotificationType type) {
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("reservationDate", reservation.getReservationDate().toString());
        notificationData.put("reservationTime", reservation.getReservationTime().toString());
        notificationData.put("restaurantName", reservation.getRestaurant().getName());
        notificationData.put("customerName", reservation.getMember().getName());
        notificationData.put("type", type);
        return notificationData;
    }

}
