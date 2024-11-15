package com.zero.bwtableback.restaurant.dto;

import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
public class ReservationSettingReqDto {
    private LocalDate startDate;
    private LocalDate endDate;
    private Long restaurantId;
    private List<WeekdaySettingDto> weekdaySettings;
}
