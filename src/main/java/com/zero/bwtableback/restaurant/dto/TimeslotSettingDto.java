package com.zero.bwtableback.restaurant.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalTime;

@Getter
@Builder
public class TimeslotSettingDto {
    private LocalTime timeslot;
    private int maxCapacity;
}
