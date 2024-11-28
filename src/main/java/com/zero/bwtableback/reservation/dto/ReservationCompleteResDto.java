package com.zero.bwtableback.reservation.dto;

import com.zero.bwtableback.restaurant.dto.RestaurantResDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReservationCompleteResDto {
    RestaurantResDto restaurant;
    ReservationResDto reservation;
}
