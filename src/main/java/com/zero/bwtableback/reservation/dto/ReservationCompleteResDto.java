package com.zero.bwtableback.reservation.dto;

import com.zero.bwtableback.restaurant.dto.RestaurantResDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ReservationCompleteResDto {
    RestaurantResDto restaurant;
    ReservationResDto reservation;
    Long chatRoomId;
}
