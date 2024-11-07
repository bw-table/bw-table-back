package com.zero.bwtableback.reservation.repository;

import com.zero.bwtableback.reservation.entity.Reservation;
import com.zero.bwtableback.reservation.entity.ReservationStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;

public class ReservationSpecifications {

    public static Specification<Reservation> hasRestaurantId(Long restaurantId) {
        return Optional.ofNullable(restaurantId)
                .map(id -> (Specification<Reservation>) (root, query, criteriaBuilder) ->
                        criteriaBuilder.equal(root.get("restaurant").get("id"), id))
                .orElse(null);
    }

    public static Specification<Reservation> hasMemberId(Long memberId) {
        return Optional.ofNullable(memberId)
                .map(id -> (Specification<Reservation>) (root, query, criteriaBuilder) ->
                        criteriaBuilder.equal(root.get("member").get("id"), id))
                .orElse(null);
    }

    public static Specification<Reservation> hasReservationStatus(ReservationStatus reservationStatus) {
        return Optional.ofNullable(reservationStatus)
                .map(status -> (Specification<Reservation>) (root, query, criteriaBuilder) ->
                        criteriaBuilder.equal(root.get("reservationStatus"), status))
                .orElse(null);
    }

    public static Specification<Reservation> hasReservationDate(LocalDate reservationDate) {
        return Optional.ofNullable(reservationDate)
                .map(date -> (Specification<Reservation>) (root, query, criteriaBuilder) ->
                        criteriaBuilder.equal(root.get("reservationDate"), date))
                .orElse(null);
    }

    public static Specification<Reservation> hasReservationTime(LocalTime reservationTime) {
        return Optional.ofNullable(reservationTime)
                .map(time -> (Specification<Reservation>) (root, query, criteriaBuilder) ->
                        criteriaBuilder.equal(root.get("reservationTime"), time))
                .orElse(null);
    }

}
