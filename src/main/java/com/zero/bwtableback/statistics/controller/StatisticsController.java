package com.zero.bwtableback.statistics.controller;

import com.zero.bwtableback.statistics.dto.StatisticsDto;
import com.zero.bwtableback.statistics.service.StatisticsService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/{restaurantId}/daily")
    public List<StatisticsDto> getDailyReservationsLast30Days(@PathVariable Long restaurantId) {
        return statisticsService.getDailyReservationsLast30Days(restaurantId);
    }

    @GetMapping("/{restaurantId}/weekly")
    public List<StatisticsDto> getWeeklyReservationsLast12Weeks(@PathVariable Long restaurantId) {
        return statisticsService.getWeeklyReservationsLast12Weeks(restaurantId);
    }

    @GetMapping("/{restaurantId}/monthly")
    public List<StatisticsDto> getMonthlyReservationsLast6Months(@PathVariable Long restaurantId) {
        return statisticsService.getMonthlyReservationsLast6Months(restaurantId);
    }

    @GetMapping("/{restaurantId}/popular-times")
    public List<StatisticsDto> getPopularTimesLast30Days(@PathVariable Long restaurantId) {
        return statisticsService.getPopularTimesLast30Days(restaurantId);
    }

    @GetMapping("/{restaurantId}/popular-dates")
    public List<StatisticsDto> getTop5PopularDatesLast30Days(@PathVariable Long restaurantId) {
        return statisticsService.getTop5PopularDatesLast30Days(restaurantId);
    }

}
