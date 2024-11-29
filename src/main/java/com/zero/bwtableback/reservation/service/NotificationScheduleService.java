package com.zero.bwtableback.reservation.service;

import static com.zero.bwtableback.reservation.entity.NotificationStatus.SENT;

import com.zero.bwtableback.common.exception.CustomException;
import com.zero.bwtableback.common.exception.ErrorCode;
import com.zero.bwtableback.reservation.entity.Notification;
import com.zero.bwtableback.reservation.entity.NotificationStatus;
import com.zero.bwtableback.reservation.entity.NotificationType;
import com.zero.bwtableback.reservation.entity.Reservation;
import com.zero.bwtableback.reservation.entity.ReservationStatus;
import com.zero.bwtableback.reservation.repository.NotificationRepository;
import com.zero.bwtableback.reservation.util.NotificationMessageGenerator;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class NotificationScheduleService {

    private final NotificationRepository notificationRepository;
    private final TaskScheduler taskScheduler;
    private final NotificationEmitterService notificationEmitterService;

    // 예약 확정 및 취소 시 즉시 알림 생성
    public void scheduleImmediateNotification(Reservation reservation, NotificationType type) {
        Notification notification = createAndSaveNotification(reservation, type);
        sendNotification(notification);
    }

    // 예약 24시간 전 알림 생성
    public void schedule24HoursBeforeNotification(Reservation reservation) {
        LocalDateTime scheduledTime = reservation.getReservationDate()
                .atTime(reservation.getReservationTime()).minusHours(24);

        if (scheduledTime.isBefore(LocalDateTime.now())) {
            log.info("예약 시간이 24시간 이내여서 알림을 스케줄링하지 않습니다.");
            return;
        }

        taskScheduler.schedule(() -> {
            if (reservation.getReservationStatus() != ReservationStatus.OWNER_CANCELED &&
                    reservation.getReservationStatus() != ReservationStatus.CUSTOMER_CANCELED) {
                sendNotification(createAndSaveNotification(reservation, NotificationType.REMINDER_24H));
            }
        }, scheduledTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    // 알림 생성 및 저장
    public Notification createAndSaveNotification(Reservation reservation, NotificationType type) {
        String message = NotificationMessageGenerator.generateMessage(reservation, type);

        Notification notification = Notification.builder()
                .reservation(reservation)
                .notificationType(type)
                .message(message)
                .status(NotificationStatus.PENDING)
                .build();

        return notificationRepository.save(notification);
    }

    // 가게 주인과 고객에게 알림 전송
    public void sendNotification(Notification notification) {
        Long guestId = notification.getReservation().getMember().getId();
        Long ownerId = notification.getReservation().getRestaurant().getMember().getId();
        notificationEmitterService.sendNotificationToGuestAndOwner(guestId, ownerId, notification);
        markAsSent(notification);
    }

    // 알림 전송 완료 상태로 업데이트
    private void markAsSent(Notification notification) {
        if (notification.getStatus() == SENT) {
            throw new CustomException(ErrorCode.NOTIFICATION_ALREADY_SENT);
        }

        notification.setSentTime(LocalDateTime.now());
        notification.setStatus(SENT);
    }

}
