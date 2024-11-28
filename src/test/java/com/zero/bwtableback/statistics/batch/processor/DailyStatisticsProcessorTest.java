package com.zero.bwtableback.statistics.batch.processor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zero.bwtableback.reservation.repository.ReservationRepository;
import com.zero.bwtableback.restaurant.entity.Restaurant;
import com.zero.bwtableback.statistics.entity.Statistics;
import com.zero.bwtableback.statistics.entity.StatisticsType;
import com.zero.bwtableback.statistics.repository.StatisticsRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DailyStatisticsProcessorTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private StatisticsRepository statisticsRepository;

    @InjectMocks
    private DailyStatisticsProcessor dailyStatisticsProcessor;

    @DisplayName("기존 통계가 없는 경우 초기 일별 통계를 생성한다")
    @Test
    void givenNoDailyStatistics_whenProcessing_thenCreateInitialDailyStatistics() {
        // given
        Restaurant restaurant = new Restaurant();
        restaurant.setId(1L);

        LocalDate today = LocalDate.now();
        LocalDate endDate = today.minusDays(1);
        LocalDate startDate = endDate.minusDays(30);
        List<Object[]> mockResults = List.of(new Object[]{startDate, 5}, new Object[]{endDate, 10});

        when(statisticsRepository.existsByRestaurantIdAndType(restaurant.getId(), StatisticsType.DAILY)).thenReturn(false);
        when(reservationRepository.aggregateDailyStatistics(restaurant.getId(), startDate, endDate)).thenReturn(mockResults);

        // when
        List<Statistics> result = dailyStatisticsProcessor.process(restaurant);

        // then
        verify(statisticsRepository).existsByRestaurantIdAndType(restaurant.getId(), StatisticsType.DAILY);
        verify(reservationRepository).aggregateDailyStatistics(restaurant.getId(), startDate, endDate);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Statistics::getTimeKey)
                .containsExactlyInAnyOrder(startDate.toString(), endDate.toString());
        assertThat(result).extracting(Statistics::getReservationCount)
                .containsExactlyInAnyOrder(5, 10);
    }

    @DisplayName("기존 통계가 있는 경우 증분 일별 통계를 업데이트한다")
    @Test
    void givenExistingDailyStatistics_whenProcessingDailyStatistics_thenUpdateIncrementalDailyStatistics() {
        // given
        Restaurant restaurant = new Restaurant();
        restaurant.setId(1L);

        LocalDate today = LocalDate.now();
        LocalDate endDate = today.minusDays(1);
        List<Object[]> mockResults = new ArrayList<>();
        mockResults.add(new Object[]{endDate, 8});

        when(statisticsRepository.existsByRestaurantIdAndType(restaurant.getId(), StatisticsType.DAILY)).thenReturn(true);
        when(reservationRepository.aggregateDailyStatistics(restaurant.getId(), endDate, endDate)).thenReturn(mockResults);

        // when
        List<Statistics> result = dailyStatisticsProcessor.process(restaurant);

        // then
        verify(statisticsRepository).existsByRestaurantIdAndType(restaurant.getId(), StatisticsType.DAILY);
        verify(reservationRepository).aggregateDailyStatistics(restaurant.getId(), endDate, endDate);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTimeKey()).isEqualTo(endDate.toString());
        assertThat(result.get(0).getReservationCount()).isEqualTo(8);
    }

}
