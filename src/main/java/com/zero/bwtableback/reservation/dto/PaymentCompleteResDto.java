package com.zero.bwtableback.reservation.dto;

import com.zero.bwtableback.reservation.entity.Reservation;
import com.zero.bwtableback.restaurant.dto.RestaurantDetailDto;

import java.time.LocalDate;
import java.time.LocalTime;

public record PaymentCompleteResDto(
        ReservationInfo reservation
) {
    public static PaymentCompleteResDto fromEntities(RestaurantDetailDto restaurantDetailDto, Reservation reservation) {
        return new PaymentCompleteResDto(
                new ReservationInfo(
                        reservation.getId(),
                        reservation.getReservationDate(),
                        reservation.getReservationTime(),
                        reservation.getNumberOfPeople(),
                        reservation.getSpecialRequest(),
                        restaurantDetailDto)
        );
    }

    record ReservationInfo(
            Long reservationId,
            LocalDate reservationDate,
            LocalTime reservationTime,
            int numberOfPeople,
            String specialRequest,
            RestaurantDetailDto restaurantDetailDto
    ) {
    }
}
