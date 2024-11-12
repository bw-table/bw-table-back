package com.zero.bwtableback.restaurant.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalTime;

@Getter
public class TimeslotSettingDto {
    private LocalTime timeslot;
    private int maxCapacity;
}
