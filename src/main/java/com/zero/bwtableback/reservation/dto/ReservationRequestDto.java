package com.zero.bwtableback.reservation.dto;

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

    public ReservationRequestDto { // TODO: 커스텀 예외 설정으로 변경 필요
        if (numberOfPeople < 1) {
            throw new IllegalArgumentException("인원 설정은 최소 한 명입니다.");
        }
        if (reservationDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("현재보다 과거의 날짜는 예약이 불가합니다.");
        }
        if (reservationTime.isBefore(LocalTime.now())) {
            throw new IllegalArgumentException("현재보다 과거의 시간은 예약이 불가합니다.");
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
