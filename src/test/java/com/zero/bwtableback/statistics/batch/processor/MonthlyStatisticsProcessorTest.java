package com.zero.bwtableback.statistics.batch.processor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MonthlyStatisticsProcessorTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private StatisticsRepository statisticsRepository;

    @InjectMocks
    private MonthlyStatisticsProcessor monthlyStatisticsProcessor;

    @DisplayName("기존 통계가 없는 경우 초기 월별 통계를 생성한다")
    @Test
    void givenNoMonthlyStatistics_whenProcessing_thenCreateInitialMonthlyStatistics() {
        // Given
        Restaurant restaurant = new Restaurant();
        restaurant.setId(1L);

        LocalDate today = LocalDate.now();
        LocalDate endDate = DateRangeCalculator.getEndDateForMonthlyPeriod(today);
        LocalDate startDate = DateRangeCalculator.getStartDateSixMonthsAgo(endDate);
        List<Object[]> mockResults = List.of(
                new Object[]{startDate, 15},
                new Object[]{endDate, 25}
        );

        when(statisticsRepository.existsByRestaurantIdAndType(restaurant.getId(), StatisticsType.MONTHLY)).thenReturn(false);
        when(reservationRepository.aggregateDailyStatistics(restaurant.getId(), startDate, endDate)).thenReturn(mockResults);

        // When
        List<Statistics> result = monthlyStatisticsProcessor.process(restaurant);

        // Then
        verify(statisticsRepository).existsByRestaurantIdAndType(restaurant.getId(), StatisticsType.MONTHLY);
        verify(reservationRepository).aggregateDailyStatistics(restaurant.getId(), startDate, endDate);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Statistics::getTimeKey)
                .containsExactlyInAnyOrder(
                        TimeKeyGenerator.generateMonthlyKey(startDate),
                        TimeKeyGenerator.generateMonthlyKey(endDate)
                );
        assertThat(result).extracting(Statistics::getReservationCount)
                .containsExactlyInAnyOrder(15, 25);
    }

    @DisplayName("기존 통계가 있는 경우 증분 월별 통계를 업데이트한다")
    @Test
    void givenExistingMonthlyStatistics_whenProcessing_thenUpdateIncrementalMonthlyStatistics() {
        // Given
        Restaurant restaurant = new Restaurant();
        restaurant.setId(1L);

        LocalDate today = LocalDate.now();
        LocalDate lastMonthEndDate = today.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
        LocalDate lastMonthStartDate = lastMonthEndDate.with(TemporalAdjusters.firstDayOfMonth());

        List<Object[]> mockResults = List.of(
                new Object[]{lastMonthEndDate, 12},
                new Object[]{lastMonthStartDate, 20}
        );

        when(statisticsRepository.existsByRestaurantIdAndType(restaurant.getId(), StatisticsType.MONTHLY)).thenReturn(true);
        when(reservationRepository.aggregateDailyStatistics(restaurant.getId(), lastMonthStartDate, lastMonthEndDate))
                .thenReturn(mockResults);

        // When
        List<Statistics> result = monthlyStatisticsProcessor.process(restaurant);

        // Then
        verify(statisticsRepository).existsByRestaurantIdAndType(restaurant.getId(), StatisticsType.MONTHLY);
        verify(reservationRepository).aggregateDailyStatistics(restaurant.getId(), lastMonthStartDate, lastMonthEndDate);

        assertThat(result).hasSize(1);
        assertThat(result).extracting(Statistics::getTimeKey)
                .containsExactlyInAnyOrder(
                        TimeKeyGenerator.generateMonthlyKey(lastMonthStartDate)
                );
        assertThat(result).extracting(Statistics::getReservationCount)
                .containsExactlyInAnyOrder(32);
    }

}
