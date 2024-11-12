package com.zero.bwtableback.restaurant.dto;

import lombok.Builder;

@Builder
public class ReservationSettingResDto {

    private Long id;
    private Long restaurantId;
    private String message;
}
