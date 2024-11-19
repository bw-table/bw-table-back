package com.zero.bwtableback.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;
import static org.mockito.BDDMockito.verify;

import com.zero.bwtableback.common.exception.CustomException;
import com.zero.bwtableback.common.exception.ErrorCode;
import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.member.repository.MemberRepository;
import com.zero.bwtableback.reservation.dto.PaymentCompleteResDto;
import com.zero.bwtableback.reservation.dto.ReservationResDto;
import com.zero.bwtableback.reservation.dto.ReservationUpdateReqDto;
import com.zero.bwtableback.reservation.entity.NotificationType;
import com.zero.bwtableback.reservation.entity.Reservation;
import com.zero.bwtableback.reservation.entity.ReservationStatus;
import com.zero.bwtableback.reservation.repository.ReservationRepository;
import com.zero.bwtableback.restaurant.dto.RestaurantInfoDto;
import com.zero.bwtableback.restaurant.entity.Restaurant;
import com.zero.bwtableback.restaurant.service.RestaurantService;

import java.io.IOException;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @InjectMocks
    private ReservationService reservationService;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private NotificationScheduleService notificationScheduleService;

    @Mock
    private RestaurantService restaurantService;

    @DisplayName("예약 확정 시 알림이 전송된다")
    @Test
    void givenPendingReservation_whenConfirmReservation_thenSendConfirmationNotifications() {
        // given
        Long reservationId = 1L;
        Long restaurantId = 2L;
        Long memberId = 3L;

        Reservation reservation = mock(Reservation.class);
        Member member = mock(Member.class);

        given(reservation.getReservationStatus()).willReturn(ReservationStatus.CUSTOMER_CANCELED);
        given(reservation.getMember()).willReturn(member);
        given(reservationRepository.findById(reservationId)).willReturn(java.util.Optional.of(reservation));
        given(memberRepository.findById(memberId)).willReturn(java.util.Optional.of(member));

        RestaurantInfoDto restaurantInfoDto = mock(RestaurantInfoDto.class);
        given(restaurantService.getRestaurantById(restaurantId)).willReturn(restaurantInfoDto);
        given(member.getId()).willReturn(memberId);

        // when
        PaymentCompleteResDto result = reservationService.confirmReservation(reservationId, memberId);

        // then
        verify(notificationScheduleService).scheduleImmediateNotification(reservation, NotificationType.CONFIRMATION);
        verify(notificationScheduleService).schedule24HoursBeforeNotification(reservation);
        assertThat(result).isNotNull();
    }

    @DisplayName("이미 확정된 예약을 다시 확정하려고 할 때 예외가 발생한다")
    @Test
    void givenConfirmedReservation_whenConfirmReservation_thenThrowException() {
        // given
        Long reservationId = 1L;
        Long restaurantId = 2L;
        Long memberId = 3L;

        Reservation reservation = mock(Reservation.class);
        Member member = mock(Member.class);

        given(reservation.getReservationStatus()).willReturn(ReservationStatus.CONFIRMED);
        given(reservation.getMember()).willReturn(member);
        given(reservationRepository.findById(reservationId)).willReturn(java.util.Optional.of(reservation));
        given(memberRepository.findById(memberId)).willReturn(java.util.Optional.of(member));
        given(member.getId()).willReturn(memberId);

        // when & then
        assertThatThrownBy(() -> reservationService.confirmReservation(reservationId, memberId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_STATUS_CONFIRM);
    }

    @DisplayName("예약일 3일 전일 경우 고객이 예약을 취소할 수 있다")
    @Test
    void givenConfirmedReservation_whenCustomerCancelWithin3Days_thenCancelSuccessfully() throws IOException {
        // given
        Long reservationId = 1L;
        Long restaurantId = 2L;
        Long memberId = 3L;

        Reservation reservation = mock(Reservation.class);
        Restaurant restaurant = mock(Restaurant.class);
        Member member = mock(Member.class);

        given(reservation.getReservationStatus()).willReturn(ReservationStatus.CONFIRMED);
        given(reservation.getReservationDate()).willReturn(LocalDate.now().plusDays(4));
        given(reservationRepository.findById(reservationId)).willReturn(java.util.Optional.of(reservation));
        given(memberRepository.findById(memberId)).willReturn(java.util.Optional.of(member));

        // Reservation에 Restaurant와 Member 설정
        given(reservation.getRestaurant()).willReturn(restaurant);
        given(restaurant.getId()).willReturn(restaurantId);

        given(reservation.getMember()).willReturn(member);
        given(member.getId()).willReturn(memberId);

        ReservationUpdateReqDto updateDto = new ReservationUpdateReqDto(restaurantId, ReservationStatus.CUSTOMER_CANCELED);

        // when
        ReservationResDto result = reservationService.updateReservationStatus(updateDto, reservationId, memberId);

        // then
        verify(notificationScheduleService).scheduleImmediateNotification(reservation, NotificationType.CANCELLATION);
        assertThat(result).isNotNull();
    }

    @DisplayName("예약일 3일 이내에 취소 요청 시 예외가 발생한다")
    @Test
    void givenConfirmedReservation_whenCustomerCancelTooLate_thenThrowException() {
        // given
        Long reservationId = 1L;
        Long memberId = 3L;

        Reservation reservation = mock(Reservation.class);
        Member member = mock(Member.class);

        given(reservation.getReservationStatus()).willReturn(ReservationStatus.CONFIRMED);
        given(reservation.getReservationDate()).willReturn(LocalDate.now().plusDays(2));
        given(reservation.getMember()).willReturn(member);
        given(reservationRepository.findById(reservationId)).willReturn(java.util.Optional.of(reservation));
        given(memberRepository.findById(memberId)).willReturn(java.util.Optional.of(member));
        given(member.getId()).willReturn(memberId);

        ReservationUpdateReqDto updateDto = new ReservationUpdateReqDto(reservationId, ReservationStatus.CUSTOMER_CANCELED);

        // when & then
        assertThatThrownBy(() -> reservationService.updateReservationStatus(updateDto, reservationId, memberId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CUSTOMER_CANCEL_TOO_LATE);
    }

    @DisplayName("잘못된 예약 상태로 업데이트하려고 할 때 예외가 발생한다")
    @Test
    void givenInvalidStatus_whenUpdateReservationStatus_thenThrowException() {
        // given
        Long reservationId = 1L;
        Long restaurantId = 2L;
        Long memberId = 3L;

        Reservation reservation = mock(Reservation.class);
        Restaurant restaurant = mock(Restaurant.class);
        Member member = mock(Member.class);

        given(reservation.getMember()).willReturn(member);
        given(reservationRepository.findById(reservationId)).willReturn(java.util.Optional.of(reservation));
        given(memberRepository.findById(memberId)).willReturn(java.util.Optional.of(member));
        given(member.getId()).willReturn(memberId);

        ReservationUpdateReqDto updateDto = new ReservationUpdateReqDto(restaurantId, null);

        // when & then
        assertThatThrownBy(() -> reservationService.updateReservationStatus(updateDto, reservationId, memberId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_RESERVATION_STATUS);
    }

}
