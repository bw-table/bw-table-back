package com.zero.bwtableback.restaurant.dto;

import com.zero.bwtableback.restaurant.entity.DayOfWeek;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
public class OperatingHoursDto {

    private DayOfWeek dayOfWeek;
    private LocalTime openingTime;
    private LocalTime closingTime;
}
