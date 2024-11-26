package com.zero.bwtableback.statistics.dto;

import com.zero.bwtableback.statistics.entity.Statistics;
import java.util.List;

public record StatisticsDto(String periodKey, int reservationCount) {

    public static List<StatisticsDto> fromEntities(List<Statistics> statisticsList) {
        return statisticsList.stream()
                .map(StatisticsDto::fromEntity)
                .toList();
    }

    private static StatisticsDto fromEntity(Statistics statistics) {
        return new StatisticsDto(
                statistics.getTimeKey(),
                statistics.getReservationCount()
        );
    }

}
