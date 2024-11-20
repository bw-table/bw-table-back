package com.zero.bwtableback.statistics.batch.processor;

import com.zero.bwtableback.reservation.repository.ReservationRepository;
import com.zero.bwtableback.restaurant.entity.Restaurant;
import com.zero.bwtableback.statistics.entity.Statistics;
import com.zero.bwtableback.statistics.entity.StatisticsType;
import com.zero.bwtableback.statistics.util.DateRangeCalculator;
import com.zero.bwtableback.statistics.util.PeriodKeyGenerator;
import java.time.LocalDate;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class WeeklyStatisticsProcessor implements ItemProcessor<Restaurant, List<Statistics>> {

    private final ReservationRepository reservationRepository;

    @Override
    public List<Statistics> process(@NonNull Restaurant restaurant) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = DateRangeCalculator.getEndOfWeekRange(today);
        LocalDate startDate = DateRangeCalculator.getStartOfWeekRange(endDate, 12);

        List<Object[]> results = reservationRepository.aggregateWeeklyStatistics(restaurant.getId(), startDate, endDate);
        return results.stream()
                .map(result -> Statistics.builder()
                        .restaurant(restaurant)
                        .type(StatisticsType.WEEKLY)
                        .timeKey(PeriodKeyGenerator.generateWeeklyKey((LocalDate) result[0])) // YYYY-Wxx
                        .reservationCount(Integer.parseInt(result[1].toString()))
                        .build()
                ).toList();
    }
}
