package com.zero.bwtableback.reservation.dto;

import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.reservation.entity.Reservation;
import com.zero.bwtableback.restaurant.entity.Restaurant;
import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationCreateReqDto(
        Long restaurantId,
        Long memberId,
        LocalDate reservationDate,
        LocalTime reservationTime,
        int numberOfPeople,
        String specialRequest
) {
    public static Reservation toEntity(ReservationCreateReqDto dto, Restaurant restaurant, Member member) {
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
