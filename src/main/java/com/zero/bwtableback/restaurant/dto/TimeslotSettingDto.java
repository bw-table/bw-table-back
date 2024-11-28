package com.zero.bwtableback.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TimeslotSettingDto {
    private LocalTime timeslot;
    private int maxCapacity;
}
