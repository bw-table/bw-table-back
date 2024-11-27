package com.zero.bwtableback.reservation.controller;

import com.zero.bwtableback.reservation.dto.NotificationResDto;
import com.zero.bwtableback.reservation.entity.NotificationType;
import com.zero.bwtableback.reservation.entity.Reservation;
import com.zero.bwtableback.reservation.service.NotificationEmitterService;
import com.zero.bwtableback.reservation.service.NotificationSearchService;
import com.zero.bwtableback.reservation.service.ReservationService;
import com.zero.bwtableback.security.MemberDetails;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final ReservationService reservationService;
    private final NotificationEmitterService notificationEmitterService;
    private final NotificationSearchService notificationSearchService;

    @PreAuthorize("hasRole('GUEST') or hasRole('OWNER')")
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@AuthenticationPrincipal MemberDetails memberDetails,
                                @RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "")
                                String lastEventId) {
        return notificationEmitterService.subscribe(memberDetails.getMemberId(), lastEventId);
    }

    @PreAuthorize("hasRole('GUEST')")
    @GetMapping("/guests")
    public Page<NotificationResDto> getGuestNotifications(@AuthenticationPrincipal MemberDetails memberDetails,
                                                          Pageable pageable) {
        return notificationSearchService.getNotificationsSentToGuest(memberDetails.getMemberId(), pageable);
    }

    @PreAuthorize("hasRole('OWNER')")
    @GetMapping("/owners")
    public Page<NotificationResDto> getOwnerNotifications(@AuthenticationPrincipal MemberDetails memberDetails,
                                                          Pageable pageable) {
        return notificationSearchService.getNotificationsSentToOwner(memberDetails.getMemberId(), pageable);
    }

    @PreAuthorize("hasRole('GUEST') or hasRole('OWNER')")
    @GetMapping("/reservations/{reservationId}/data")
    public Map<String, Object> getNotificationDataForMessage(@AuthenticationPrincipal MemberDetails memberDetails,
                                                             @PathVariable Long reservationId,
                                                             @RequestParam NotificationType type) {
        Reservation reservation = reservationService.findReservationById(reservationId);
        return notificationSearchService.createNotificationData(memberDetails.getMemberId(), reservation, type);
    }

}
