package com.zero.bwtableback.statistics.controller;

import com.zero.bwtableback.security.MemberDetails;
import com.zero.bwtableback.statistics.dto.StatisticsDto;
import com.zero.bwtableback.statistics.service.StatisticsService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@PreAuthorize("hasRole('OWNER')")
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/{restaurantId}/daily")
    public List<StatisticsDto> getDailyReservationsLast30Days(@PathVariable Long restaurantId,
                                                              @AuthenticationPrincipal MemberDetails memberDetails) {
        return statisticsService.getDailyReservationsLast30Days(restaurantId, memberDetails.getMemberId());
    }

    @GetMapping("/{restaurantId}/weekly")
    public List<StatisticsDto> getWeeklyReservationsLast12Weeks(@PathVariable Long restaurantId,
                                                                @AuthenticationPrincipal MemberDetails memberDetails) {
        return statisticsService.getWeeklyReservationsLast12Weeks(restaurantId, memberDetails.getMemberId());
    }

    @GetMapping("/{restaurantId}/monthly")
    public List<StatisticsDto> getMonthlyReservationsLast6Months(@PathVariable Long restaurantId,
                                                                 @AuthenticationPrincipal MemberDetails memberDetails) {
        return statisticsService.getMonthlyReservationsLast6Months(restaurantId, memberDetails.getMemberId());
    }

    @GetMapping("/{restaurantId}/popular-times")
    public List<StatisticsDto> getPopularTimesLast30Days(@PathVariable Long restaurantId,
                                                         @AuthenticationPrincipal MemberDetails memberDetails) {
        return statisticsService.getPopularTimesLast30Days(restaurantId, memberDetails.getMemberId());
    }

    @GetMapping("/{restaurantId}/popular-dates")
    public List<StatisticsDto> getTop5PopularDatesLast30Days(@PathVariable Long restaurantId,
                                                             @AuthenticationPrincipal MemberDetails memberDetails) {
        return statisticsService.getTop5PopularDatesLast30Days(restaurantId, memberDetails.getMemberId());
    }

}
