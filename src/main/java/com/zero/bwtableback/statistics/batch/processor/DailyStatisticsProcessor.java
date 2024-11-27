package com.zero.bwtableback.statistics.batch.processor;

import com.zero.bwtableback.reservation.repository.ReservationRepository;
import com.zero.bwtableback.restaurant.entity.Restaurant;
import com.zero.bwtableback.statistics.entity.Statistics;
import com.zero.bwtableback.statistics.entity.StatisticsType;
import com.zero.bwtableback.statistics.repository.StatisticsRepository;
import com.zero.bwtableback.statistics.util.TimeKeyGenerator;
import java.time.LocalDate;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class DailyStatisticsProcessor implements ItemProcessor<Restaurant, List<Statistics>> {

    private final ReservationRepository reservationRepository;
    private final StatisticsRepository statisticsRepository;

    @Override
    public List<Statistics> process(@NonNull Restaurant restaurant) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.minusDays(1);
        LocalDate startDate = endDate.minusDays(30);

        boolean hasDailyStatistics = statisticsRepository.existsByRestaurantIdAndType(
                restaurant.getId(), StatisticsType.DAILY);

        if (!hasDailyStatistics) {
            return createInitialDailyStatistics(restaurant, startDate, endDate);
        }

        return updateDailyStatistics(restaurant, endDate);
    }

    private List<Statistics> createInitialDailyStatistics(Restaurant restaurant, LocalDate startDate, LocalDate endDate) {
        List<Object[]> results = reservationRepository.aggregateDailyStatistics(restaurant.getId(), startDate, endDate);
        return mapResultsToStatistics(results, restaurant);
    }

    private List<Statistics> updateDailyStatistics(Restaurant restaurant, LocalDate endDate) {
        List<Object[]> results = reservationRepository.aggregateDailyStatistics(restaurant.getId(), endDate, endDate);
        return mapResultsToStatistics(results, restaurant);
    }

    private List<Statistics> mapResultsToStatistics(List<Object[]> results, Restaurant restaurant) {
        return results.stream()
                .map(result -> Statistics.builder()
                        .restaurant(restaurant)
                        .type(StatisticsType.DAILY)
                        .timeKey(TimeKeyGenerator.generateDailyKey((LocalDate) result[0])) // YYYY-MM-DD
                        .reservationCount(Integer.parseInt(result[1].toString()))
                        .build()
                ).toList();
    }

}
