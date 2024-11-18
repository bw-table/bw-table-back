package com.zero.bwtableback.reservation.repository;

import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.reservation.entity.Reservation;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.zero.bwtableback.reservation.entity.ReservationStatus;
import com.zero.bwtableback.restaurant.entity.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ReservationRepository extends JpaRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {

    List<Reservation> findByReservationDate(LocalDate reservationDate);

    Page<Reservation> findByMemberId(Long memberId, Pageable pageable);

    Optional<Reservation> findByMemberAndRestaurantAndReservationStatus(
            Member member, Restaurant restaurant, ReservationStatus reservationStatus);
}
