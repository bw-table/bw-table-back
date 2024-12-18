package com.zero.bwtableback.statistics.batch.processor;

import com.zero.bwtableback.reservation.repository.ReservationRepository;
import com.zero.bwtableback.restaurant.entity.Restaurant;
import com.zero.bwtableback.statistics.entity.Statistics;
import com.zero.bwtableback.statistics.entity.StatisticsType;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PopularTimeSlotsProcessor implements ItemProcessor<Restaurant, List<Statistics>> {

    private final ReservationRepository reservationRepository;

    @Override
    public List<Statistics> process(@NonNull Restaurant restaurant) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.minusDays(1);
        LocalDate startDate = endDate.minusDays(30);

        List<Object[]> results = reservationRepository.aggregateTimeSlotStatistics(restaurant.getId(), startDate, endDate);
        return mapResultsToStatistics(results, restaurant);
    }

    private List<Statistics> mapResultsToStatistics(List<Object[]> results, Restaurant restaurant) {
        return results.stream()
                .sorted(Comparator.comparingInt(result -> -Integer.parseInt(result[1].toString()))) // 예약 건수를 비교해서 내림차순 정렬
                .map(result -> Statistics.builder()
                        .restaurant(restaurant)
                        .type(StatisticsType.TIME_SLOT)
                        .timeKey(result[0].toString())
                        .reservationCount(Integer.parseInt(result[1].toString()))
                        .build())
                .toList();
    }

}
