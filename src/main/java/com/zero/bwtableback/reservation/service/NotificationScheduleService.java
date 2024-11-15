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
import com.zero.bwtableback.reservation.repository.ReservationRepository;
import com.zero.bwtableback.reservation.util.NotificationMessageGenerator;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class NotificationScheduleService {

    private final NotificationRepository notificationRepository;
    private final TaskScheduler taskScheduler;
//    private final NotificationEmitterService notificationEmitterService;
    private final ReservationRepository reservationRepository;

    // 예약 확정 및 취소 시 즉시 알림 생성
    public void scheduleImmediateNotification(Reservation reservation, NotificationType type) {
        Notification notification = createAndSaveNotification(reservation, type);
        sendNotification(notification);
    }

    // 가게 주인과 고객에게 알림 전송
    public void sendNotification(Notification notification) {
        Long customerId = notification.getReservation().getMember().getId();
        Long ownerId = notification.getReservation().getRestaurant().getMember().getId();
//        notificationEmitterService.sendNotificationToCustomerAndOwner(customerId, ownerId, notification);
        markAsSent(notification);
        notification.setSentTime(LocalDateTime.now());
    }

    // 예약 24시간 전 알림 생성
    public void schedule24HoursBeforeNotification(Reservation reservation) {
        LocalDateTime scheduledTime = reservation.getReservationDate()
                .atTime(reservation.getReservationTime()).minusHours(24);
        createScheduledNotification(reservation, scheduledTime);
    }

    // 예약 상태 확인 후 알림 전송 및 상태 업데이트
    private void createScheduledNotification(Reservation reservation, LocalDateTime scheduledTime) {
        Notification notification = createAndSaveNotification(reservation, NotificationType.REMINDER_24H);
        Long customerId = notification.getReservation().getMember().getId();
        Long ownerId = notification.getReservation().getRestaurant().getMember().getId();

        // 스케줄링된 시간에 전송 상태로 변경하고 알림 전송
        taskScheduler.schedule(() -> {
            // FIXME
//            if (reservation.getReservationStatus() != ReservationStatus.OWNER_CANCELED &&
//                    reservation.getReservationStatus() != ReservationStatus.CUSTOMER_CANCELED) {
//                notificationEmitterService.sendNotificationToCustomerAndOwner(customerId, ownerId, notification);
//                markAsSent(notification);
//                notification.setSentTime(LocalDateTime.now());
//            }
        }, scheduledTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    // 알림 생성 및 저장
    public Notification createAndSaveNotification(Reservation reservation, NotificationType type) {
        String message = NotificationMessageGenerator.generateMessage(reservation, type);

        Notification notification = Notification.builder()
                .reservation(reservation)
                .notificationType(type)
                .message(message)
                .scheduledTime(LocalDateTime.now())
                .status(NotificationStatus.PENDING)
                .build();

        return notificationRepository.save(notification);
    }

    // 알림 전송 완료 상태로 업데이트
    private void markAsSent(Notification notification) {
        if (notification.getStatus() == SENT) {
            throw new CustomException(ErrorCode.NOTIFICATION_ALREADY_SENT);
        }
        if (LocalDateTime.now().isBefore(notification.getScheduledTime())) {
            throw new CustomException(ErrorCode.NOTIFICATION_SCHEDULED_TIME_NOT_REACHED);
        }

        notification.setSentTime(LocalDateTime.now());
        notification.setStatus(SENT);
    }

    // 특정 날짜의 예약 목록 조회
    public List<Reservation> findReservationsByDate(LocalDate date) {
        return reservationRepository.findByReservationDate(date);
    }

    // 일주일 지난 알림 목록 조회
    public List<Notification> findOldNotifications(LocalDateTime cutoffDate, NotificationStatus status) {
        return notificationRepository.findBySentTimeBeforeAndStatus(cutoffDate, status);
    }

    // 일주일 지난 알림 리스트 일괄 삭제
    public void deleteNotifications(List<Notification> notifications) {
        notificationRepository.deleteAllInBatch(notifications);
    }

}
