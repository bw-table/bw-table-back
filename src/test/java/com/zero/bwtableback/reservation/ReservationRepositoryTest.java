package com.zero.bwtableback.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import com.zero.bwtableback.reservation.entity.Reservation;
import com.zero.bwtableback.reservation.entity.ReservationStatus;
import com.zero.bwtableback.reservation.repository.ReservationRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@DataJpaTest
public class ReservationRepositoryTest {

    @Autowired
    ReservationRepository reservationRepository;

    private LocalDate testDate;
    private LocalTime testTime;

    @BeforeEach
    public void setup() {

        // 테스트용 날짜와 시간 설정
        testDate = LocalDate.of(2024, 11, 1);
        testTime = LocalTime.of(18, 0);

        // 테스트 데이터 삽입
        Reservation reservation1 = Reservation.builder()
                .reservationDate(testDate)
                .reservationTime(testTime)
                .numberOfPeople(4)
                .specialRequests("Vegetarian")
                .reservationStatus(ReservationStatus.CONFIRMED)
                .build();
        reservationRepository.save(reservation1);

        Reservation reservation2 = Reservation.builder()
                .reservationDate(testDate)
                .reservationTime(LocalTime.of(19, 0))
                .numberOfPeople(2)
                .specialRequests("Less salty")
                .reservationStatus(ReservationStatus.CONFIRMED)
                .build();
        reservationRepository.save(reservation2);

        Reservation reservation3 = Reservation.builder()
                .reservationDate(LocalDate.of(2024, 11, 2))
                .reservationTime(testTime)
                .numberOfPeople(3)
                .specialRequests("Near entrance")
                .reservationStatus(ReservationStatus.WAITING)
                .build();
        reservationRepository.save(reservation3);

    }

    @DisplayName("특정 날짜와 시간으로 예약을 검색하면 정보가 정확히 반환된다")
    @Test
    public void givenReservationExistsForDateAndTime_whenFindingByDateAndTime_thenReturnMatchingReservation() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Reservation> result = reservationRepository.findByReservationDateAndReservationTime(testDate, testTime, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1); // 조회된 예약 건수가 1건인지 확인
        Reservation reservation = result.getContent().get(0); // 내용 일치 확인
        assertThat(reservation.getReservationDate()).isEqualTo(testDate);
        assertThat(reservation.getReservationTime()).isEqualTo(testTime);
        assertThat(reservation.getNumberOfPeople()).isEqualTo(4);
        assertThat(reservation.getSpecialRequests()).isEqualTo("Vegetarian");
        assertThat(reservation.getReservationStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    }
}
