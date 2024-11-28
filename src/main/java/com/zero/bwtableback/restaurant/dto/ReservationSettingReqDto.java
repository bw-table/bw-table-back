package com.zero.bwtableback.restaurant.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
public class ReservationSettingReqDto {
    private LocalDate startDate;
    private LocalDate endDate;
    private List<WeekdaySettingDto> weekdaySettings;
    @Setter
    private Long restaurantId;
}
