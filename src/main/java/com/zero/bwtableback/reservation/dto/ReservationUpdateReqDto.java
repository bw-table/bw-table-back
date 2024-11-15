package com.zero.bwtableback.reservation.dto;

import com.zero.bwtableback.reservation.entity.ReservationStatus;

public record ReservationUpdateReqDto(Long restaurantId, ReservationStatus reservationStatus) {
}
