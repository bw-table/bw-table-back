package com.zero.bwtableback.restaurant.dto;

import com.zero.bwtableback.restaurant.entity.DayOfWeek;
import com.zero.bwtableback.restaurant.entity.OperatingHours;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OperatingHoursDto {

    private Long id;
    private DayOfWeek dayOfWeek;
    private LocalTime openingTime;
    private LocalTime closingTime;
    private Long restaurantId;
}
