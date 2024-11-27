package com.zero.bwtableback.statistics.batch.processor;

import com.zero.bwtableback.reservation.repository.ReservationRepository;
import com.zero.bwtableback.restaurant.entity.Restaurant;
import com.zero.bwtableback.statistics.entity.Statistics;
import com.zero.bwtableback.statistics.entity.StatisticsType;
import com.zero.bwtableback.statistics.repository.StatisticsRepository;
import com.zero.bwtableback.statistics.util.DateRangeCalculator;
import com.zero.bwtableback.statistics.util.TimeKeyGenerator;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class WeeklyStatisticsProcessor implements ItemProcessor<Restaurant, List<Statistics>> {

    private final ReservationRepository reservationRepository;
    private final StatisticsRepository statisticsRepository;

    @Override
    public List<Statistics> process(@NonNull Restaurant restaurant) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = DateRangeCalculator.getEndDateOfWeek(today);
        LocalDate startDate = DateRangeCalculator.getStartOfWeek(endDate, 12);

        boolean hasWeeklyStatistics = statisticsRepository.existsByRestaurantIdAndType(
                restaurant.getId(), StatisticsType.WEEKLY);

        if (!hasWeeklyStatistics) {
            return createInitialWeeklyStatistics(restaurant, startDate, endDate);
        }

        return updateWeeklyStatistics(restaurant, today);
    }

    private List<Statistics> createInitialWeeklyStatistics(Restaurant restaurant, LocalDate startDate, LocalDate endDate) {
        List<Object[]> results = reservationRepository.aggregateDailyStatistics(restaurant.getId(), startDate, endDate);
        return aggregateWeeklyStatistics(results, restaurant);
    }

    private List<Statistics> updateWeeklyStatistics(Restaurant restaurant, LocalDate today) {
        LocalDate lastWeekEndDate = DateRangeCalculator.getEndDateOfWeek(today);
        LocalDate lastWeekStartDate = DateRangeCalculator.getStartOfWeek(lastWeekEndDate, 0);

        List<Object[]> results = reservationRepository.aggregateDailyStatistics(
                restaurant.getId(), lastWeekStartDate, lastWeekEndDate);

        return aggregateWeeklyStatistics(results, restaurant);
    }

    private List<Statistics> aggregateWeeklyStatistics(List<Object[]> results, Restaurant restaurant) {
        return results.stream()
                .collect(Collectors.groupingBy(
                        result -> TimeKeyGenerator.generateWeeklyKey(
                                DateRangeCalculator.getStartOfWeek((LocalDate) result[0], 0),
                                DateRangeCalculator.getEndDateOfWeek((LocalDate) result[0])
                        ),
                        Collectors.summingInt(result -> Integer.parseInt(result[1].toString()))
                ))
                .entrySet()
                .stream()
                .map(entry -> Statistics.builder()
                        .restaurant(restaurant)
                        .type(StatisticsType.WEEKLY)
                        .timeKey(entry.getKey())
                        .reservationCount(entry.getValue())
                        .build()
                )
                .toList();
    }
}
