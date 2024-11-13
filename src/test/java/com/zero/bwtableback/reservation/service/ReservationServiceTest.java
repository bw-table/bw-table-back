package com.zero.bwtableback.reservation.service;

import static com.zero.bwtableback.restaurant.entity.CategoryType.KOREAN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.zero.bwtableback.common.exception.CustomException;
import com.zero.bwtableback.member.entity.LoginType;
import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.member.entity.Role;
import com.zero.bwtableback.member.repository.MemberRepository;
import com.zero.bwtableback.reservation.dto.ReservationReqDto;
import com.zero.bwtableback.reservation.dto.ReservationResDto;
import com.zero.bwtableback.reservation.entity.Reservation;
import com.zero.bwtableback.reservation.entity.ReservationStatus;
import com.zero.bwtableback.reservation.repository.ReservationRepository;
import com.zero.bwtableback.restaurant.entity.Category;
import com.zero.bwtableback.restaurant.entity.Restaurant;
import com.zero.bwtableback.restaurant.repository.RestaurantRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private NotificationScheduleService notificationScheduleService;  // Mock 추가

    @InjectMocks
    private ReservationService reservationService;

    private Reservation mockReservation;

    private Restaurant mockRestaurant;

    private Member mockMember;

    @BeforeEach
    void setup() {
        // Mock Restaurant 설정
        mockRestaurant = Restaurant.builder()
                .id(1L)
                .name("Mock Restaurant")
                .description("Mock Description")
                .address("123 Mock Street")
                .latitude(37.1234)
                .longitude(127.5678)
                .contact("010-1234-5678")
                .closedDay("Monday")
                .operatingHours(List.of())
                .info("Please follow our guidelines.")
                .link("http://mockrestaurant.com")
                .images(Set.of())
                .category(new Category(1L, KOREAN, 0, List.of()))
                .menus(List.of())
                .facilities(List.of())
                .hashtags(List.of())
                .averageRating(4.5)
                .build();

        // Mock Member 설정
        mockMember = Member.builder()
                .id(1L)
                .loginType(LoginType.EMAIL)
                .email("testuser@example.com")
                .password("password123")
                .name("Test User")
                .nickname("TestNickname")
                .phone("010-1234-5678")
                .role(Role.GUEST)
                .businessNumber("123-45-67890")
                .profileImage("http://example.com/profile.jpg")
                .provider("Kakao")
                .providerId("provider-12345")
                .build();

        // Mock Reservation 설정
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

    @DisplayName("예약 ID로 예약을 조회할 때 존재하면 예약 정보를 반환한다")
    @Test
    void givenReservationId_whenGetReservationById_thenReturnReservation() {
        // given
        given(reservationRepository.findById(1L)).willReturn(Optional.of(mockReservation));

        // when
        ReservationResDto result = reservationService.getReservationById(1L);

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
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("해당 예약을 찾을 수 없습니다");
        then(reservationRepository).should().findById(1L);
    }

    @DisplayName("새로운 예약을 생성할 수 있다")
    @Test
    void givenReservationRequest_whenCreateReservation_thenSaveAndReturnReservation() {
        // given
        ReservationReqDto requestDto = new ReservationReqDto(
                1L, 1L, LocalDate.now(),
                LocalTime.now().plusHours(1), 4, "Special request");
        given(reservationRepository.save(any())).willReturn(mockReservation);
        given(restaurantRepository.findById(1L)).willReturn(Optional.of(mockRestaurant));
        given(memberRepository.findById(1L)).willReturn(Optional.of(mockMember)); // 추가


        // when
        ReservationResDto result = reservationService.createReservation(
                requestDto, mockRestaurant.getId(), mockMember.getId());

        // then
        assertThat(result.reservationId()).isEqualTo(mockReservation.getId());
        then(reservationRepository).should().save(any());
        then(restaurantRepository).should().findById(1L);
        then(memberRepository).should().findById(1L);
    }

    @DisplayName("확정된 예약을 다시 확정하려고 하면 예외를 발생시킨다")
    @Test
    void givenConfirmedReservation_whenConfirmReservation_thenThrowException() {
        // given
        mockReservation.setReservationStatus(ReservationStatus.CONFIRMED);  // 예약 상태를 CONFIRMED로 설정
        given(reservationRepository.findById(1L)).willReturn(Optional.of(mockReservation));

        // when & then
        assertThatThrownBy(() -> reservationService.confirmReservation(1L, 1L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("이미 확정되었거나, 취소, 노쇼, 방문 완료된 예약은 다시 확정할 수 없습니다.");
        then(reservationRepository).should().findById(1L);  // Verify findById was called
    }

    @DisplayName("고객이 확정된 예약을 취소할 수 있다")
    @Test
    void givenConfirmedReservation_whenCancelReservationByCustomer_thenChangeStatusToCustomerCanceled() {
        // given
        given(reservationRepository.findById(1L)).willReturn(Optional.of(mockReservation));

        // when
        ReservationResDto result = reservationService.cancelReservationByCustomer(1L);

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
        ReservationResDto result = reservationService.cancelReservationByOwner(1L);

        // then
        assertThat(result.reservationStatus()).isEqualTo(ReservationStatus.OWNER_CANCELED);
        then(reservationRepository).should().findById(1L);
    }

    @DisplayName("확정된 예약을 노쇼로 상태 변경이 가능하다")
    @Test
    void givenConfirmedReservation_whenMarkAsNoShow_thenChangeStatusToNoShow() {
        // given
        given(reservationRepository.findById(1L)).willReturn(Optional.of(mockReservation));

        // when
        ReservationResDto result = reservationService.markReservationAsNoShow(1L);

        // then
        assertThat(result.reservationStatus()).isEqualTo(ReservationStatus.NO_SHOW);
        then(reservationRepository).should().findById(1L);
    }

    @DisplayName("확정된 예약을 방문 완료로 상태 변경이 가능하다")
    @Test
    void givenConfirmedReservation_whenMarkAsVisited_thenChangeStatusToVisited() {
        // given
        given(reservationRepository.findById(1L)).willReturn(Optional.of(mockReservation));

        // when
        ReservationResDto result = reservationService.markReservationAsVisited(1L);

        // then
        assertThat(result.reservationStatus()).isEqualTo(ReservationStatus.VISITED);
        then(reservationRepository).should().findById(1L);
    }
}
