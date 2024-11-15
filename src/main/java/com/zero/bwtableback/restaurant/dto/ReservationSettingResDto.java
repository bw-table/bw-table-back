package com.zero.bwtableback.restaurant.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReservationSettingResDto {

    private Long id;
    private Long restaurantId;
    private String message;
}
