package com.zero.bwtableback.reservation.controller;

import com.zero.bwtableback.reservation.dto.NotificationResDto;
import com.zero.bwtableback.reservation.entity.Notification;
import com.zero.bwtableback.reservation.entity.NotificationType;
import com.zero.bwtableback.reservation.entity.Reservation;
import com.zero.bwtableback.reservation.service.NotificationEmitterService;
import com.zero.bwtableback.reservation.service.NotificationSearchService;
import com.zero.bwtableback.reservation.service.ReservationService;
import com.zero.bwtableback.security.MemberDetails;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final ReservationService reservationService;
    private final NotificationEmitterService notificationEmitterService;
    private final NotificationSearchService notificationSearchService;

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@AuthenticationPrincipal MemberDetails memberDetails,
                                @RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "")
                                String lastEventId) {
        return notificationEmitterService.subscribe(memberDetails.getMemberId(), lastEventId);
    }

    // 고객 회원에게 전송된 알림 목록 조회
    @GetMapping("/customers/{userId}")
    public Page<NotificationResDto> getCustomerNotifications(@PathVariable Long userId, Pageable pageable) {
        Page<Notification> notifications = notificationSearchService.getNotificationsSentToCustomer(userId, pageable);
        return notifications.map(NotificationResDto::fromEntity);
    }

    // 가게 주인 회원에게 전송된 알림 목록 조회
    @GetMapping("/owners/{ownerId}")
    public Page<NotificationResDto> getOwnerNotifications(@PathVariable Long ownerId, Pageable pageable) {
        Page<Notification> notifications = notificationSearchService.getNotificationsSentToOwner(ownerId, pageable);
        return notifications.map(NotificationResDto::fromEntity);
    }

    // 메시지 생성에 사용할 수 있는 알림 정보 반환
    @GetMapping("/reservations/{reservationId}/data")
    public Map<String, Object> createNotificationData(@PathVariable Long reservationId,
                                                      @RequestParam NotificationType type) {
        Reservation reservation = reservationService.findReservationById(reservationId);
        return notificationSearchService.createNotificationData(reservation, type);
    }

}
