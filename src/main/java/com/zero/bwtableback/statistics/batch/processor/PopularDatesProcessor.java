package com.zero.bwtableback.statistics.batch.processor;

import com.zero.bwtableback.reservation.repository.ReservationRepository;
import com.zero.bwtableback.restaurant.entity.Restaurant;
import com.zero.bwtableback.statistics.entity.Statistics;
import com.zero.bwtableback.statistics.entity.StatisticsType;
import com.zero.bwtableback.statistics.util.PeriodKeyGenerator;
import java.time.LocalDate;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PopularDatesProcessor implements ItemProcessor<Restaurant, List<Statistics>> {

    private final ReservationRepository reservationRepository;

    @Override
    public List<Statistics> process(@NonNull Restaurant restaurant) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.minusDays(1);
        LocalDate startDate = endDate.minusDays(30);

        List<Object[]> results = reservationRepository.aggregateDailyStatistics(restaurant.getId(), startDate, endDate);
        return results.stream()
                .sorted((a, b) -> Integer.compare((int) b[1], (int) a[1])) // 예약 건수를 비교해서 내림차순 정렬
                .limit(5) // 상위 5개의 인기 일자만 선택
                .map(result -> Statistics.builder()
                        .restaurant(restaurant)
                        .type(StatisticsType.POPULAR_DATE)
                        .timeKey(PeriodKeyGenerator.generateDailyKey((LocalDate) result[0]))
                        .reservationCount(Integer.parseInt(result[1].toString()))
                        .build()
                ).toList();
    }
}
