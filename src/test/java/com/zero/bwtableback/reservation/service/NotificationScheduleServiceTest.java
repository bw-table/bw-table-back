package com.zero.bwtableback.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.zero.bwtableback.common.exception.CustomException;
import com.zero.bwtableback.common.exception.ErrorCode;
import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.reservation.entity.Notification;
import com.zero.bwtableback.reservation.entity.NotificationStatus;
import com.zero.bwtableback.reservation.entity.NotificationType;
import com.zero.bwtableback.reservation.entity.Reservation;
import com.zero.bwtableback.reservation.repository.NotificationRepository;
import com.zero.bwtableback.reservation.util.NotificationMessageGenerator;
import com.zero.bwtableback.restaurant.entity.Restaurant;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.TaskScheduler;

@ExtendWith(MockitoExtension.class)
class NotificationScheduleServiceTest {

    @InjectMocks
    private NotificationScheduleService notificationScheduleService;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private TaskScheduler taskScheduler;

    @Mock
    private NotificationEmitterService notificationEmitterService;

    @DisplayName("예약 확정 시 즉시 알림을 생성하고 전송한다")
    @Test
    void givenReservationAndType_whenScheduleImmediateNotification_thenCreateAndSendNotification() {
        // given
        Reservation reservation = mock(Reservation.class);
        Restaurant restaurant = mock(Restaurant.class);
        Member customerMember = mock(Member.class);
        Member ownerMember = mock(Member.class);
        Notification notification = mock(Notification.class);
        String message = "알림 메시지 예시";

        LocalDateTime scheduledTime = LocalDateTime.now().minusMinutes(5); // 과거 시간 설정

        given(reservation.getMember()).willReturn(customerMember);
        given(reservation.getRestaurant()).willReturn(restaurant);
        given(restaurant.getMember()).willReturn(ownerMember);
        given(customerMember.getId()).willReturn(1L);
        given(ownerMember.getId()).willReturn(2L);
        given(notification.getReservation()).willReturn(reservation);
        given(notification.getScheduledTime()).willReturn(scheduledTime);

        try (MockedStatic<NotificationMessageGenerator> mockedGenerator = Mockito.mockStatic(NotificationMessageGenerator.class)) {
            mockedGenerator.when(() -> NotificationMessageGenerator.generateMessage(reservation, NotificationType.CONFIRMATION))
                    .thenReturn(message);

            given(notificationRepository.save(any(Notification.class))).willReturn(notification);
            doNothing().when(notificationEmitterService).sendNotificationToCustomerAndOwner(any(), any(), any());

            // when
            notificationScheduleService.scheduleImmediateNotification(reservation, NotificationType.CONFIRMATION);

            // then
            verify(notificationRepository).save(any(Notification.class));
            verify(notificationEmitterService).sendNotificationToCustomerAndOwner(any(), any(), any());
            verify(notification).setSentTime(any(LocalDateTime.class));
        }
    }

    @DisplayName("예약 24시간 전 알림을 스케줄링한다")
    @Test
    void givenReservation_whenSchedule24HoursBeforeNotification_thenScheduleNotification() {
        // given
        Reservation reservation = mock(Reservation.class);
        Member customerMember = mock(Member.class);
        Member ownerMember = mock(Member.class);
        Restaurant restaurant = mock(Restaurant.class);

        given(reservation.getMember()).willReturn(customerMember);
        given(reservation.getRestaurant()).willReturn(restaurant);
        given(restaurant.getMember()).willReturn(ownerMember);
        given(ownerMember.getId()).willReturn(1L);

        // 예약 날짜와 시간을 설정
        LocalDate reservationDate = LocalDate.now().plusDays(1);
        LocalTime reservationTime = LocalTime.of(12, 0);
        LocalDateTime reservationDateTime = LocalDateTime.of(reservationDate, reservationTime);
        LocalDateTime expectedScheduleTime = reservationDateTime.minusHours(24);

        given(reservation.getReservationDate()).willReturn(reservationDate);
        given(reservation.getReservationTime()).willReturn(reservationTime);

        try (MockedStatic<NotificationMessageGenerator> mockedGenerator = Mockito.mockStatic(NotificationMessageGenerator.class)) {
            mockedGenerator.when(() -> NotificationMessageGenerator.generateMessage(any(Reservation.class), any(NotificationType.class)))
                    .thenReturn("알림 메시지 예시");

            Notification notification = Notification.builder()
                    .reservation(reservation)
                    .notificationType(NotificationType.REMINDER_24H)
                    .message("알림 메시지 예시")
                    .scheduledTime(LocalDateTime.now())
                    .status(NotificationStatus.PENDING)
                    .build();

            given(notificationRepository.save(any(Notification.class))).willReturn(notification);

            ArgumentCaptor<Instant> instantCaptor = ArgumentCaptor.forClass(Instant.class);

            // when
            notificationScheduleService.schedule24HoursBeforeNotification(reservation);

            // then
            verify(taskScheduler).schedule(any(Runnable.class), instantCaptor.capture());

            Instant capturedInstant = instantCaptor.getValue();
            Instant expectedInstant = expectedScheduleTime.atZone(ZoneId.systemDefault()).toInstant();

            assertThat(capturedInstant).isEqualTo(expectedInstant);
        }
    }

    @DisplayName("예약 상태에 따라 알림을 생성하고 저장한다")
    @Test
    void givenReservationAndNotificationType_whenCreateAndSaveNotification_thenNotificationIsSaved() {
        // given
        Reservation reservation = mock(Reservation.class);
        NotificationType type = NotificationType.REMINDER_24H;
        String message = "알림 메시지 예시";

        try (MockedStatic<NotificationMessageGenerator> mockedGenerator = Mockito.mockStatic(NotificationMessageGenerator.class)) {
            mockedGenerator.when(() -> NotificationMessageGenerator.generateMessage(reservation, type)).thenReturn(message);

            Notification notification = Notification.builder()
                    .reservation(reservation)
                    .notificationType(type)
                    .message(message)
                    .scheduledTime(LocalDateTime.now())
                    .status(NotificationStatus.PENDING)
                    .build();

            given(notificationRepository.save(any(Notification.class))).willReturn(notification);

            // when
            Notification savedNotification = notificationScheduleService.createAndSaveNotification(reservation, type);

            // then
            assertThat(savedNotification).isNotNull();
            assertThat(savedNotification.getMessage()).isEqualTo(message);
            verify(notificationRepository).save(any(Notification.class));
        }
    }

    @DisplayName("이미 전송된 알림을 다시 전송 시도하면 예외를 발생시킨다")
    @Test
    void givenAlreadySentNotification_whenImmediateNotificationScheduled_thenThrowException() {
        // given
        Reservation reservation = mock(Reservation.class);
        Member customerMember = mock(Member.class);
        Member ownerMember = mock(Member.class);
        Restaurant restaurant = mock(Restaurant.class);

        given(reservation.getMember()).willReturn(customerMember);
        given(reservation.getRestaurant()).willReturn(restaurant);
        given(restaurant.getMember()).willReturn(ownerMember);

        Notification notification = Notification.builder()
                .reservation(reservation)
                .notificationType(NotificationType.CONFIRMATION)
                .message("알림 메시지 예시")
                .scheduledTime(LocalDateTime.now())
                .status(NotificationStatus.SENT) // 이미 전송된 상태로 설정
                .build();

        given(notificationRepository.save(any(Notification.class))).willReturn(notification);

        try (MockedStatic<NotificationMessageGenerator> mockedGenerator = Mockito.mockStatic(NotificationMessageGenerator.class)) {
            mockedGenerator.when(() -> NotificationMessageGenerator.generateMessage(any(Reservation.class), any(NotificationType.class)))
                    .thenReturn("알림 메시지 예시");

            // when & then
            assertThatThrownBy(() -> notificationScheduleService.scheduleImmediateNotification(reservation, NotificationType.CONFIRMATION))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOTIFICATION_ALREADY_SENT);
        }
    }

    @DisplayName("예정된 시간 전에 알림 전송을 시도하면 예외를 발생시킨다")
    @Test
    void givenNotificationWithFutureScheduledTime_whenImmediateNotificationScheduled_thenThrowException() {
        // given
        Reservation reservation = mock(Reservation.class);
        Member customerMember = mock(Member.class);
        Member ownerMember = mock(Member.class);
        Restaurant restaurant = mock(Restaurant.class);

        given(reservation.getMember()).willReturn(customerMember);
        given(reservation.getRestaurant()).willReturn(restaurant);
        given(restaurant.getMember()).willReturn(ownerMember);

        // 미래 시간으로 예약 시간을 설정
        LocalDateTime futureScheduledTime = LocalDateTime.now().plusMinutes(10);

        Notification notification = Notification.builder()
                .reservation(reservation)
                .notificationType(NotificationType.CONFIRMATION)
                .message("알림 메시지 예시")
                .scheduledTime(futureScheduledTime)
                .status(NotificationStatus.PENDING) // 아직 보내지 않은 알림으로 설정
                .build();

        given(notificationRepository.save(any(Notification.class))).willReturn(notification);

        try (MockedStatic<NotificationMessageGenerator> mockedGenerator = Mockito.mockStatic(NotificationMessageGenerator.class)) {
            mockedGenerator.when(() -> NotificationMessageGenerator.generateMessage(any(Reservation.class), any(NotificationType.class)))
                    .thenReturn("알림 메시지 예시");

            // when & then
            assertThatThrownBy(() -> notificationScheduleService.scheduleImmediateNotification(reservation, NotificationType.CONFIRMATION))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOTIFICATION_SCHEDULED_TIME_NOT_REACHED);
        }
    }

}
