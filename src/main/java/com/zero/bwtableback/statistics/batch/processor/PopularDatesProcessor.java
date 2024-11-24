package com.zero.bwtableback.statistics.batch.processor;

import com.zero.bwtableback.reservation.repository.ReservationRepository;
import com.zero.bwtableback.restaurant.entity.Restaurant;
import com.zero.bwtableback.statistics.entity.Statistics;
import com.zero.bwtableback.statistics.entity.StatisticsType;
import com.zero.bwtableback.statistics.repository.StatisticsRepository;
import com.zero.bwtableback.statistics.util.TimeKeyGenerator;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PopularDatesProcessor implements ItemProcessor<Restaurant, List<Statistics>> {

    private final ReservationRepository reservationRepository;
    private final StatisticsRepository statisticsRepository;

    @Override
    public List<Statistics> process(@NonNull Restaurant restaurant) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.minusDays(1);
        LocalDate startDate = endDate.minusDays(30);

        boolean hasPopularDatesStatistics = statisticsRepository.existsByRestaurantIdAndType(
                restaurant.getId(), StatisticsType.POPULAR_DATE);

        if (!hasPopularDatesStatistics) {
            return createInitialPopularDatesStatistics(restaurant, startDate, endDate);
        }

        return updatePopularDatesStatistics(restaurant, endDate);
    }

    private List<Statistics> createInitialPopularDatesStatistics(Restaurant restaurant, LocalDate startDate, LocalDate endDate) {
        List<Object[]> results = reservationRepository.aggregateDailyStatistics(restaurant.getId(), startDate, endDate);
        return mapResultsToPopularDatesStatistics(results, restaurant);
    }

    private List<Statistics> updatePopularDatesStatistics(Restaurant restaurant, LocalDate endDate) {
        List<Object[]> results = reservationRepository.aggregateDailyStatistics(restaurant.getId(), endDate, endDate);
        return mapResultsToPopularDatesStatistics(results, restaurant);
    }

    private List<Statistics> mapResultsToPopularDatesStatistics(List<Object[]> results, Restaurant restaurant) {
        return results.stream()
                .sorted(Comparator.comparingInt(result -> -Integer.parseInt(result[1].toString()))) // 예약 건수를 비교해서 내림차순 정렬
                .limit(5) // 상위 5개의 인기 일자만 선택
                .map(result -> Statistics.builder()
                        .restaurant(restaurant)
                        .type(StatisticsType.POPULAR_DATE)
                        .timeKey(TimeKeyGenerator.generateDailyKey((LocalDate) result[0]))
                        .reservationCount(Integer.parseInt(result[1].toString()))
                        .build()
                ).toList();
    }
}
