package com.zero.bwtableback.reservation.repository;

import com.zero.bwtableback.reservation.entity.Reservation;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    //    Page<Reservation> findByRestaurantName(String restaurantName, Pageable pageable);

    Page<Reservation> findByReservationDateAndReservationTime(LocalDate date, LocalTime time, Pageable pageable);

    //    Page<Reservation> findByRestaurantNameAndDate(String restaurantName, LocalDate date, Pageable pageable);

}
