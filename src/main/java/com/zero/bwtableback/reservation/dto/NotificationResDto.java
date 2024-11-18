package com.zero.bwtableback.reservation.dto;

import com.zero.bwtableback.reservation.entity.Notification;
import com.zero.bwtableback.reservation.entity.NotificationType;
import com.zero.bwtableback.reservation.entity.NotificationStatus;
import java.time.LocalDateTime;

public record NotificationResDto(
        Long id,
        Long reservationId,
        NotificationType notificationType,
        String message
) {
    public static NotificationResDto fromEntity(Notification notification) {
        return new NotificationResDto(
                notification.getId(),
                notification.getReservation().getId(),
                notification.getNotificationType(),
                notification.getMessage()
        );
    }
}
