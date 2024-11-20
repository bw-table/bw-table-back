package com.zero.bwtableback.reservation.dto;

import com.zero.bwtableback.common.exception.CustomException;
import com.zero.bwtableback.common.exception.ErrorCode;
import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.reservation.entity.Reservation;
import com.zero.bwtableback.restaurant.entity.Restaurant;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationRequestDto(
        Long restaurantId,
        Long memberId,
        LocalDate reservationDate,
        LocalTime reservationTime,
        int numberOfPeople,
        String specialRequest
) {

    public ReservationRequestDto {
        if (numberOfPeople < 1) {
            throw new CustomException(ErrorCode.INVALID_PEOPLE_COUNT);
        }
        if (reservationDate.isBefore(LocalDate.now())) {
            throw new CustomException(ErrorCode.INVALID_RESERVATION_DATE);
        }
        if (reservationTime.isBefore(LocalTime.now())) {
            throw new CustomException(ErrorCode.INVALID_RESERVATION_TIME);
        }

    }

    public static Reservation toEntity(ReservationRequestDto dto, Restaurant restaurant, Member member) {
        return Reservation.builder()
                .restaurant(restaurant)
                .member(member)
                .reservationDate(dto.reservationDate)
                .reservationTime(dto.reservationTime)
                .numberOfPeople(dto.numberOfPeople)
                .specialRequest(dto.specialRequest)
                .build();
    }

}
