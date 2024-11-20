package com.zero.bwtableback.reservation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.reservation.entity.Reservation;
import com.zero.bwtableback.restaurant.entity.Restaurant;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;

public record ReservationCreateReqDto(
        Long restaurantId,
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
