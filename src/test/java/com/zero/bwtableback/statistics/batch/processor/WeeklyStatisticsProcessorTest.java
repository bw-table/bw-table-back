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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WeeklyStatisticsProcessorTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private StatisticsRepository statisticsRepository;

    @InjectMocks
    private WeeklyStatisticsProcessor weeklyStatisticsProcessor;


    @DisplayName("기존 주별 통계가 없는 경우 초기 데이터를 생성한다")
    @Test
    void givenNoWeeklyStatistics_whenProcessing_thenCreateInitialWeeklyStatistics() {
        // Given
        Restaurant restaurant = new Restaurant();
        restaurant.setId(1L);

        LocalDate today = LocalDate.now();
        LocalDate endDate = DateRangeCalculator.getEndDateOfWeek(today); // 마지막 주 마지막 날
        LocalDate startDate = DateRangeCalculator.getStartOfWeek(endDate, 12); // 12주 전 첫 날

        List<Object[]> mockResults = List.of(
                new Object[]{startDate.plusDays(1), 5},
                new Object[]{endDate.minusDays(2), 10}
        );

        when(statisticsRepository.existsByRestaurantIdAndType(restaurant.getId(), StatisticsType.WEEKLY))
                .thenReturn(false);
        when(reservationRepository.aggregateDailyStatistics(restaurant.getId(), startDate, endDate))
                .thenReturn(mockResults);

        // When
        List<Statistics> result = weeklyStatisticsProcessor.process(restaurant);

        // Then
        verify(statisticsRepository).existsByRestaurantIdAndType(restaurant.getId(), StatisticsType.WEEKLY);
        verify(reservationRepository).aggregateDailyStatistics(restaurant.getId(), startDate, endDate);

        // 검증: 결과 값만 비교
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Statistics::getTimeKey)
                .containsExactlyInAnyOrder(
                        TimeKeyGenerator.generateWeeklyKey( // 마지막 주 마지막 날
                                DateRangeCalculator.getStartOfWeek(startDate.plusDays(1), 0),
                                DateRangeCalculator.getEndDateOfWeek(startDate.plusDays(1))
                        ),
                        TimeKeyGenerator.generateWeeklyKey( // 12주 전 첫 날
                                DateRangeCalculator.getStartOfWeek(endDate.minusDays(2), 0),
                                DateRangeCalculator.getEndDateOfWeek(endDate.minusDays(2))
                        )
                );
        assertThat(result).extracting(Statistics::getReservationCount)
                .containsExactlyInAnyOrder(5, 10);
    }

    @DisplayName("기존 주별 통계가 있는 경우 증분 데이터를 업데이트한다")
    @Test
    void givenExistingWeeklyStatistics_whenProcessing_thenUpdateIncrementalWeeklyStatistics() {
        // Given
        Restaurant restaurant = new Restaurant();
        restaurant.setId(1L);

        LocalDate today = LocalDate.now();
        LocalDate lastWeekEndDate = DateRangeCalculator.getEndDateOfWeek(today);
        LocalDate lastWeekStartDate = DateRangeCalculator.getStartOfWeek(lastWeekEndDate, 0);

        List<Object[]> mockResults = List.of(
                new Object[]{lastWeekStartDate.plusDays(1), 8}, // 지난 주 월요일
                new Object[]{lastWeekEndDate.minusDays(2), 12} // 지난 주 목요일
        );

        when(statisticsRepository.existsByRestaurantIdAndType(restaurant.getId(), StatisticsType.WEEKLY))
                .thenReturn(true);
        when(reservationRepository.aggregateDailyStatistics(restaurant.getId(), lastWeekStartDate, lastWeekEndDate))
                .thenReturn(mockResults);

        // When
        List<Statistics> result = weeklyStatisticsProcessor.process(restaurant);

        // Then
        verify(statisticsRepository).existsByRestaurantIdAndType(restaurant.getId(), StatisticsType.WEEKLY);
        verify(reservationRepository).aggregateDailyStatistics(restaurant.getId(), lastWeekStartDate, lastWeekEndDate);

        String expectedTimeKey = TimeKeyGenerator.generateWeeklyKey(lastWeekStartDate, lastWeekEndDate);
        assertThat(result).hasSize(1);
        assertThat(result).extracting(Statistics::getTimeKey)
                .containsExactly(expectedTimeKey);
        assertThat(result).extracting(Statistics::getReservationCount)
                .containsExactly(20);
    }

    @DisplayName("주간 키가 올바르게 생성된다")
    @Test
    void testWeeklyKeyGeneration() {
        // Given
        List<Object[]> mockResults = List.of(
                new Object[]{LocalDate.of(2024, 11, 19), 10},
                new Object[]{LocalDate.of(2024, 11, 11), 5}
        );

        // When
        Map<String, Integer> aggregatedData = mockResults.stream()
                .collect(Collectors.groupingBy(
                        mockResult -> TimeKeyGenerator.generateWeeklyKey(
                                DateRangeCalculator.getStartOfWeek((LocalDate) mockResult[0], 0),
                                DateRangeCalculator.getEndDateOfWeek((LocalDate) mockResult[0])
                        ),
                        Collectors.summingInt(mockResult -> Integer.parseInt(mockResult[1].toString()))
                ));

        // Then
        assertThat(aggregatedData)
                .containsEntry("2024-11-17 ~ 2024-11-23", 10)
                .containsEntry("2024-11-10 ~ 2024-11-16", 5);
    }

}
