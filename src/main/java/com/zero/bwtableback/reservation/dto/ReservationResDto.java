package com.zero.bwtableback.reservation.dto;

import com.zero.bwtableback.reservation.entity.Reservation;
import com.zero.bwtableback.reservation.entity.ReservationStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationResDto(
        Long reservationId,
        Long restaurantId,
        Long memberId,
        LocalDate reservationDate,
        LocalTime reservationTime,
        int numberOfPeople,
        String specialRequest,
        ReservationStatus reservationStatus
) {

    public static ReservationResDto fromEntity(Reservation reservation) {
        return new ReservationResDto(
                reservation.getId(),
                reservation.getRestaurant().getId(),
                reservation.getMember().getId(),
                reservation.getReservationDate(),
                reservation.getReservationTime(),
                reservation.getNumberOfPeople(),
                reservation.getSpecialRequest(),
                reservation.getReservationStatus()
        );
    }
}
