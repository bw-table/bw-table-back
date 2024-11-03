package com.zero.bwtableback.reservation.repository;

import com.zero.bwtableback.reservation.entity.Reservation;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // 가게명으로 예약 목록을 조회
    // Page<Reservation> findByRestaurantName(String restaurantName, Pageable pageable);

    // 예약일자, 예약시간으로 예약 목록 조회
    Page<Reservation> findByReservationDateAndReservationTime(LocalDate date, LocalTime time, Pageable pageable);

    // 가게명, 예약일자로 예약 목록 조회
    // Page<Reservation> findByRestaurantNameAndDate(String restaurantName, LocalDate date, Pageable pageable);

}
