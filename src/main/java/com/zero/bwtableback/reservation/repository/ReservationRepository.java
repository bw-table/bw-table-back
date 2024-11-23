package com.zero.bwtableback.reservation.repository;

import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.reservation.dto.ReservationResDto;
import com.zero.bwtableback.reservation.entity.Reservation;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.zero.bwtableback.restaurant.entity.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("SELECT r FROM Reservation r WHERE r.reservationDate = :date")
    List<Reservation> findReservationsByDate(@Param("date") LocalDate date);

    Page<Reservation> findByMemberId(Long memberId, Pageable pageable);

    Optional<Reservation> findByMemberAndRestaurantAndReservationDateBetween(
            Member member, Restaurant restaurant, LocalDate startDate, LocalDate endDate);

    List<Reservation> findByRestaurantId(Long restaurantId);

    Page<Reservation> findByRestaurantIdAndReservationDate(Long restaurantId, LocalDate reservationDate, Pageable pageable);

    Reservation findTopByMemberOrderByReservationDateDesc(Member member);
}
