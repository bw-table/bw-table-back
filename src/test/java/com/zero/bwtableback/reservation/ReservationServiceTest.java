package com.zero.bwtableback.reservation;
import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.reservation.dto.ReservationRequestDto;
import com.zero.bwtableback.reservation.dto.ReservationResponseDto;
import com.zero.bwtableback.reservation.entity.Reservation;
import com.zero.bwtableback.reservation.entity.ReservationStatus;
import com.zero.bwtableback.reservation.repository.ReservationRepository;
import com.zero.bwtableback.reservation.service.ReservationService;
import com.zero.bwtableback.restaurant.entity.Restaurant;
import org.junit.jupiter.api.BeforeEach;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReservationService reservationService;

    private Reservation mockReservation;
    private Restaurant mockRestaurant;
    private Member mockMember;

    @BeforeEach
    void setup() {
        mockRestaurant = new Restaurant();
        mockMember = new Member();
        mockReservation = Reservation.builder()
                .id(1L)
                .restaurant(mockRestaurant)
                .member(mockMember)
                .reservationDate(LocalDate.now())
                .reservationTime(LocalTime.now())
                .numberOfPeople(4)
                .specialRequest("Special request")
                .reservationStatus(ReservationStatus.CONFIRMED)
                .build();
    }

    @DisplayName("필터를 사용해 예약 목록을 조회할 수 있다")
    @Test
    void givenFilters_whenFindReservations_thenReturnFilteredReservations() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Reservation> reservationPage = new PageImpl<>(List.of(mockReservation));
        given(reservationRepository.findAll(any(Specification.class), any(Pageable.class)))
                .willReturn(reservationPage);

        // when
        Page<ReservationResponseDto> result = reservationService.findReservationsWithFilters(
                1L, 1L, ReservationStatus.CONFIRMED, LocalDate.now(), LocalTime.now(), pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).reservationId()).isEqualTo(mockReservation.getId());
    }

    @DisplayName("예약 ID로 예약을 조회할 때 존재하면 예약 정보를 반환한다")
    @Test
    void givenReservationId_whenGetReservationById_thenReturnReservation() {
        // given
        given(reservationRepository.findById(1L)).willReturn(Optional.of(mockReservation));

        // when
        ReservationResponseDto result = reservationService.getReservationById(1L);

        // then
        assertThat(result.reservationId()).isEqualTo(mockReservation.getId());
        then(reservationRepository).should().findById(1L);
    }

    @DisplayName("예약 ID로 예약을 조회할 때 존재하지 않으면 예외를 발생시킨다")
    @Test
    void givenInvalidReservationId_whenGetReservationById_thenThrowException() {
        // given
        given(reservationRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reservationService.getReservationById(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("해당 예약을 찾을 수 없습니다");
        then(reservationRepository).should().findById(1L);
    }

    @DisplayName("새로운 예약을 생성할 수 있다")
    @Test
    void givenReservationRequest_whenCreateReservation_thenSaveAndReturnReservation() {
        // given
        ReservationRequestDto requestDto = new ReservationRequestDto(
                1L, 1L, LocalDate.now(), LocalTime.now().plusHours(1), 4, "Special request");
        given(reservationRepository.save(any())).willReturn(mockReservation);

        // when
        ReservationResponseDto result = reservationService.createReservation(requestDto, mockRestaurant, mockMember);

        // then
        assertThat(result.reservationId()).isEqualTo(mockReservation.getId());
        then(reservationRepository).should().save(any());
    }

    @DisplayName("확정된 예약을 다시 확정하려고 하면 예외를 발생시킨다")
    @Test
    void givenConfirmedReservation_whenConfirmReservation_thenThrowException() {
        // given
        given(reservationRepository.findById(1L)).willReturn(Optional.of(mockReservation));

        // when & then
        assertThatThrownBy(() -> reservationService.confirmReservation(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 확정된 예약입니다.");
    }

    @DisplayName("고객이 확정된 예약을 취소할 수 있다")
    @Test
    void givenConfirmedReservation_whenCancelReservationByCustomer_thenChangeStatusToCustomerCanceled() {
        // given
        given(reservationRepository.findById(1L)).willReturn(Optional.of(mockReservation));

        // when
        ReservationResponseDto result = reservationService.cancelReservationByCustomer(1L);

        // then
        assertThat(result.reservationStatus()).isEqualTo(ReservationStatus.CUSTOMER_CANCELED);
        then(reservationRepository).should().findById(1L);
    }

    @DisplayName("가게 측에서 확정된 예약을 취소할 수 있다")
    @Test
    void givenConfirmedReservation_whenCancelReservationByOwner_thenChangeStatusToOwnerCanceled() {
        // given
        given(reservationRepository.findById(1L)).willReturn(Optional.of(mockReservation));

        // when
        ReservationResponseDto result = reservationService.cancelReservationByOwner(1L);

        // then
        assertThat(result.reservationStatus()).isEqualTo(ReservationStatus.OWNER_CANCELED);
        then(reservationRepository).should().findById(1L);
    }

    @DisplayName("노쇼로 상태 변경이 가능하다")
    @Test
    void givenConfirmedReservation_whenMarkAsNoShow_thenChangeStatusToNoShow() {
        // given
        given(reservationRepository.findById(1L)).willReturn(Optional.of(mockReservation));

        // when
        ReservationResponseDto result = reservationService.markReservationAsNoShow(1L);

        // then
        assertThat(result.reservationStatus()).isEqualTo(ReservationStatus.NO_SHOW);
        then(reservationRepository).should().findById(1L);
    }

    @DisplayName("방문 완료로 상태 변경이 가능하다")
    @Test
    void givenConfirmedReservation_whenMarkAsVisited_thenChangeStatusToVisited() {
        // given
        given(reservationRepository.findById(1L)).willReturn(Optional.of(mockReservation));

        // when
        ReservationResponseDto result = reservationService.markReservationAsVisited(1L);

        // then
        assertThat(result.reservationStatus()).isEqualTo(ReservationStatus.VISITED);
        then(reservationRepository).should().findById(1L);
    }
}
