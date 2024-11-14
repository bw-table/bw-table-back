package com.zero.bwtableback.restaurant.dto;

import com.zero.bwtableback.restaurant.entity.DayOfWeek;
import lombok.AllArgsConstructor;
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
