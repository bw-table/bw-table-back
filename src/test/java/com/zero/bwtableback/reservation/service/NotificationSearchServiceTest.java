package com.zero.bwtableback.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.reservation.dto.NotificationResDto;
import com.zero.bwtableback.reservation.entity.Notification;
import com.zero.bwtableback.reservation.entity.NotificationStatus;
import com.zero.bwtableback.reservation.entity.NotificationType;
import com.zero.bwtableback.reservation.entity.Reservation;
import com.zero.bwtableback.reservation.entity.ReservationStatus;
import com.zero.bwtableback.reservation.repository.NotificationRepository;
import com.zero.bwtableback.restaurant.entity.Restaurant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class NotificationSearchServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private Restaurant restaurant;

    @Mock
    private Member member;

    @InjectMocks
    private NotificationSearchService notificationSearchService;

    @DisplayName("고객에게 전송된 알림 목록을 조회한다")
    @Test
    void givenGuestId_whenGetNotificationsSentToCustomer_thenReturnNotifications() {
        // given
        Long guestId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        Reservation reservation = Reservation.builder()
                .id(1L)
                .restaurant(restaurant)
                .member(member)
                .reservationDate(LocalDate.of(2024, 11, 12))
                .reservationTime(LocalTime.of(18, 0))
                .numberOfPeople(2)
                .specialRequest("None")
                .reservationStatus(ReservationStatus.CONFIRMED)
                .build();

        Notification notification = Notification.builder()
                .reservation(reservation)
                .notificationType(NotificationType.CONFIRMATION)
                .message("예약이 확정되었습니다.")
                .status(NotificationStatus.SENT)
                .build();

        Page<Notification> notifications = new PageImpl<>(Collections.singletonList(notification), pageable, 1);
        when(notificationRepository.findByReservation_Member_IdAndStatusOrderBySentTimeDesc(
                customerId, NotificationStatus.SENT, pageable))
                .thenReturn(notifications);

        // when
        Page<NotificationResDto> result = notificationSearchService.getNotificationsSentToGuest(guestId, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).message()).isEqualTo("예약이 확정되었습니다.");
    }

    @DisplayName("가게 주인에게 전송된 알림 목록을 조회한다")
    @Test
    void givenOwnerId_whenGetNotificationsSentToOwner_thenReturnNotifications() {
        // given
        Long ownerId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        Reservation reservation = Reservation.builder()
                .id(1L)
                .restaurant(restaurant)
                .member(member)
                .reservationDate(LocalDate.of(2024, 11, 12))
                .reservationTime(LocalTime.of(18, 0))
                .numberOfPeople(2)
                .specialRequest("None")
                .reservationStatus(ReservationStatus.CONFIRMED)
                .build();

        Notification notification = Notification.builder()
                .reservation(reservation)
                .notificationType(NotificationType.CONFIRMATION)
                .message("예약이 확정되었습니다.")
                .status(NotificationStatus.SENT)
                .build();

        Page<Notification> notifications = new PageImpl<>(Collections.singletonList(notification), pageable, 1);
        when(notificationRepository.findByReservation_Restaurant_Member_IdAndStatusOrderBySentTimeDesc(
                ownerId, NotificationStatus.SENT, pageable))
                .thenReturn(notifications);

        // when
        Page<NotificationResDto> result = notificationSearchService.getNotificationsSentToOwner(ownerId, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).message()).isEqualTo("예약이 확정되었습니다.");
    }

    @DisplayName("알림 내역 관련 데이터를 반환한다")
    @Test
    void givenMemberIdReservationAndNotificationType_whenCreateNotificationData_thenReturnNotificationData() {
        // given
        Long memberId = 1L;

        Reservation reservation = Reservation.builder()
                .id(1L)
                .reservationDate(LocalDate.of(2024, 11, 12))
                .reservationTime(LocalTime.of(18, 0))
                .restaurant(restaurant)
                .member(member)
                .build();

        NotificationType type = NotificationType.REMINDER_24H;

        when(restaurant.getName()).thenReturn("Test Restaurant");
        when(member.getName()).thenReturn("Test Customer");
        when(member.getId()).thenReturn(memberId);

        // when
        Map<String, Object> notificationData = notificationSearchService.createNotificationData(memberId, reservation, type);

        // then
        assertThat(notificationData).containsEntry("reservationDate", "2024-11-12");
        assertThat(notificationData).containsEntry("reservationTime", "18:00");
        assertThat(notificationData).containsEntry("restaurantName", "Test Restaurant");
        assertThat(notificationData).containsEntry("customerName", "Test Customer");
        assertThat(notificationData).containsEntry("type", type);
    }

}
