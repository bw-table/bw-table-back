package com.zero.bwtableback.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationAvailabilityDto {
    private boolean isAvailable;
    private String message;
}
