package com.zero.bwtableback.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
    void scheduleImmediateNotification_CreatesAndSendsNotification() {
        // given
        Reservation reservation = mock(Reservation.class);
        Member customerMember = mock(Member.class);
        Member ownerMember = mock(Member.class);
        Restaurant restaurant = mock(Restaurant.class);
        Notification notification = mock(Notification.class);

        String message = "알림 메시지 예시";

        given(reservation.getMember()).willReturn(customerMember);
        given(reservation.getRestaurant()).willReturn(restaurant);
        given(restaurant.getMember()).willReturn(ownerMember);
        given(customerMember.getId()).willReturn(1L);
        given(ownerMember.getId()).willReturn(2L);
        given(notification.getReservation()).willReturn(reservation);

        try (MockedStatic<NotificationMessageGenerator> mockedGenerator = Mockito.mockStatic(NotificationMessageGenerator.class)) {
            mockedGenerator.when(() -> NotificationMessageGenerator.generateMessage(reservation, NotificationType.CONFIRMATION))
                    .thenReturn(message);

            given(notificationRepository.save(any(Notification.class))).willReturn(notification);

            // when
            notificationScheduleService.scheduleImmediateNotification(reservation, NotificationType.CONFIRMATION);

            // then
            verify(notificationRepository).save(any(Notification.class));
            verify(notificationEmitterService).sendNotificationToCustomerAndOwner(1L, 2L, notification);
        }
    }

    @DisplayName("예약 24시간 전 알림을 스케줄링한다")
    @Test
    void schedule24HoursBeforeNotification_SchedulesNotificationCorrectly() {
        // given
        Reservation reservation = mock(Reservation.class);

        LocalDate reservationDate = LocalDate.now().plusDays(10);
        LocalTime reservationTime = LocalTime.of(12, 0);
        LocalDateTime reservationDateTime = LocalDateTime.of(reservationDate, reservationTime);
        LocalDateTime expectedScheduleTime = reservationDateTime.minusHours(24);

        given(reservation.getReservationDate()).willReturn(reservationDate);
        given(reservation.getReservationTime()).willReturn(reservationTime);

        try (MockedStatic<NotificationMessageGenerator> mockedGenerator = Mockito.mockStatic(NotificationMessageGenerator.class)) {
            mockedGenerator.when(() -> NotificationMessageGenerator.generateMessage(any(Reservation.class), any(NotificationType.class)))
                    .thenReturn("알림 메시지 예시");

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

    @DisplayName("이미 전송된 알림에 대해 예외를 발생시킨다")
    @Test
    void scheduleImmediateNotification_ThrowsExceptionIfAlreadySent() {
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
                .status(NotificationStatus.SENT)
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

    @DisplayName("예약 시간이 24시간 이내인 경우 알림 스케줄링을 생략한다")
    @Test
    void schedule24HoursBeforeNotification_DoesNotScheduleIfWithin24Hours() {
        // given
        Reservation reservation = mock(Reservation.class);

        LocalDate reservationDate = LocalDate.now();
        LocalTime reservationTime = LocalTime.now().plusHours(1); // 현재 시간 기준 1시간 후
        given(reservation.getReservationDate()).willReturn(reservationDate);
        given(reservation.getReservationTime()).willReturn(reservationTime);

        // when
        notificationScheduleService.schedule24HoursBeforeNotification(reservation);

        // then
        verify(notificationRepository, never()).save(any(Notification.class));
        verify(taskScheduler, never()).schedule(any(Runnable.class), any(Instant.class));
    }

}
