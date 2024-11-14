package com.zero.bwtableback.restaurant.dto;

import com.zero.bwtableback.restaurant.entity.DayOfWeek;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class WeekdaySettingDto {
    private DayOfWeek dayOfWeek;
    private List<TimeslotSettingDto> timeslotSettings;
}
