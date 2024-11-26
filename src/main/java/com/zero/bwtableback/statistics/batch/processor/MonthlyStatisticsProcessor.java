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
public class MonthlyStatisticsProcessor implements ItemProcessor<Restaurant, List<Statistics>> {

    private final ReservationRepository reservationRepository;

    @Override
    public List<Statistics> process(@NonNull Restaurant restaurant) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = DateRangeCalculator.getEndDateForMonthRange(today);
        LocalDate startDate = DateRangeCalculator.getStartDateForMonthRange(today);

        List<Object[]> results = reservationRepository.aggregateMonthlyStatistics(restaurant.getId(), startDate, endDate);
        return results.stream()
                .map(result -> Statistics.builder()
                        .restaurant(restaurant)
                        .type(StatisticsType.MONTHLY)
                        .timeKey(PeriodKeyGenerator.generateMonthlyKey((LocalDate) result[0])) // YYYY-MM
                        .reservationCount(Integer.parseInt(result[1].toString()))
                        .build()
                ).toList();
    }
}
