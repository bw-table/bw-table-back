package com.zero.bwtableback.restaurant.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class ReservationSettingDetailDto {

    private Long id;
    private Long restaurantId;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<WeekdaySettingDto> weekdaySettings;
}
