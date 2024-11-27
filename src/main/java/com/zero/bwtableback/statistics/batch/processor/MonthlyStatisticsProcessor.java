package com.zero.bwtableback.statistics.batch.processor;

import com.zero.bwtableback.reservation.repository.ReservationRepository;
import com.zero.bwtableback.restaurant.entity.Restaurant;
import com.zero.bwtableback.statistics.entity.Statistics;
import com.zero.bwtableback.statistics.entity.StatisticsType;
import com.zero.bwtableback.statistics.repository.StatisticsRepository;
import com.zero.bwtableback.statistics.util.DateRangeCalculator;
import com.zero.bwtableback.statistics.util.TimeKeyGenerator;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class MonthlyStatisticsProcessor implements ItemProcessor<Restaurant, List<Statistics>> {

    private final ReservationRepository reservationRepository;
    private final StatisticsRepository statisticsRepository;

    @Override
    public List<Statistics> process(@NonNull Restaurant restaurant) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = DateRangeCalculator.getEndDateForMonthlyPeriod(today);
        LocalDate startDate = DateRangeCalculator.getStartDateSixMonthsAgo(endDate);

        boolean hasMonthlyStatistics = statisticsRepository.existsByRestaurantIdAndType(
                restaurant.getId(), StatisticsType.MONTHLY);

        if (!hasMonthlyStatistics) {
            return createInitialMonthlyStatistics(restaurant, startDate, endDate);
        }

        return updateMonthlyStatistics(restaurant, today);
    }

    private List<Statistics> createInitialMonthlyStatistics(Restaurant restaurant, LocalDate startDate, LocalDate endDate) {
        List<Object[]> results = reservationRepository.aggregateDailyStatistics(restaurant.getId(), startDate, endDate);
        return aggregateMonthlyStatistics(results, restaurant);
    }

    private List<Statistics> updateMonthlyStatistics(Restaurant restaurant, LocalDate today) {
        LocalDate lastMonthEndDate = today.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
        LocalDate lastMonthStartDate = lastMonthEndDate.with(TemporalAdjusters.firstDayOfMonth());

        List<Object[]> results = reservationRepository.aggregateDailyStatistics(
                restaurant.getId(), lastMonthStartDate, lastMonthEndDate);

        return aggregateMonthlyStatistics(results, restaurant);
    }

    private List<Statistics> aggregateMonthlyStatistics(List<Object[]> results, Restaurant restaurant) {
        return results.stream()
                .collect(Collectors.groupingBy(
                        result -> TimeKeyGenerator.generateMonthlyKey((LocalDate) result[0]),
                        Collectors.summingInt(result -> Integer.parseInt(result[1].toString()))
                ))
                .entrySet()
                .stream()
                .map(entry -> Statistics.builder()
                        .restaurant(restaurant)
                        .type(StatisticsType.MONTHLY)
                        .timeKey(entry.getKey())
                        .reservationCount(entry.getValue())
                        .build()
                )
                .toList();
    }

}
