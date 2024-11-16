package com.zero.bwtableback.reservation.repository;

import com.zero.bwtableback.reservation.entity.Reservation;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationRepository extends JpaRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {

    List<Reservation> findByReservationDate(LocalDate reservationDate);

    Page<Reservation> findByMemberId(Long memberId, Pageable pageable);

    /**
     * 현재 예약 건수 확인
     * COALESCE는 합계가 NULL일 경우 0 반환
     */
    @Query("SELECT COALESCE(SUM(r.numberOfPeople), 0) FROM Reservation r " +
            "WHERE r.restaurant.id = :restaurantId " +
            "AND r.reservationDate = :reservationDate " +
            "AND r.reservationTime = :reservationTime " +
            "AND r.reservationStatus = 'CONFIRMED'")
    int countReservedPeopleByRestaurantAndDateTime(
            @Param("restaurantId") Long restaurantId,
            @Param("reservationDate") LocalDate reservationDate,
            @Param("reservationTime") LocalTime reservationTime
    );
}
