package com.zero.bwtableback.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate reservationDate,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
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
