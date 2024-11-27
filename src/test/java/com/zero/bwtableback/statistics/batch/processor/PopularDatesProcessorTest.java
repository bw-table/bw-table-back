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
class PopularDatesProcessorTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private StatisticsRepository statisticsRepository;

    @InjectMocks
    private PopularDatesProcessor popularDatesProcessor;

    @DisplayName("기존 인기 일자 통계가 없는 경우 초기 데이터를 생성한다")
    @Test
    void givenNoPopularDatesStatistics_whenProcessing_thenCreateInitialPopularDatesStatistics() {
        // Given
        Restaurant restaurant = new Restaurant();
        restaurant.setId(1L);

        LocalDate today = LocalDate.now();
        LocalDate endDate = today.minusDays(1);
        LocalDate startDate = endDate.minusDays(30);

        List<Object[]> mockResults = List.of(
                new Object[]{startDate.plusDays(1), 12},
                new Object[]{endDate.minusDays(2), 18},
                new Object[]{endDate.minusDays(5), 8}
        );

        when(statisticsRepository.existsByRestaurantIdAndType(restaurant.getId(), StatisticsType.POPULAR_DATE))
                .thenReturn(false);
        when(reservationRepository.aggregateDailyStatistics(restaurant.getId(), startDate, endDate))
                .thenReturn(mockResults);

        // When
        List<Statistics> result = popularDatesProcessor.process(restaurant);

        // Then
        verify(statisticsRepository).existsByRestaurantIdAndType(restaurant.getId(), StatisticsType.POPULAR_DATE);
        verify(reservationRepository).aggregateDailyStatistics(restaurant.getId(), startDate, endDate);

        assertThat(result).hasSize(3);
        assertThat(result).extracting(Statistics::getTimeKey)
                .containsExactly(
                        endDate.minusDays(2).toString(),
                        startDate.plusDays(1).toString(),
                        endDate.minusDays(5).toString()
                );
        assertThat(result).extracting(Statistics::getReservationCount)
                .containsExactly(18, 12, 8);
    }

    @DisplayName("기존 인기 일자 통계가 있는 경우 증분 데이터를 업데이트한다")
    @Test
    void givenExistingPopularDatesStatistics_whenProcessing_thenUpdatePopularDatesStatistics() {
        // Given
        Restaurant restaurant = new Restaurant();
        restaurant.setId(1L);

        LocalDate today = LocalDate.now();
        LocalDate endDate = today.minusDays(1);

        List<Object[]> mockResults = new ArrayList<>();
        mockResults.add(new Object[]{endDate, 25});

        when(statisticsRepository.existsByRestaurantIdAndType(restaurant.getId(), StatisticsType.POPULAR_DATE))
                .thenReturn(true);
        when(reservationRepository.aggregateDailyStatistics(restaurant.getId(), endDate, endDate))
                .thenReturn(mockResults);

        // When
        List<Statistics> result = popularDatesProcessor.process(restaurant);

        // Then
        verify(statisticsRepository).existsByRestaurantIdAndType(restaurant.getId(), StatisticsType.POPULAR_DATE);
        verify(reservationRepository).aggregateDailyStatistics(restaurant.getId(), endDate, endDate);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTimeKey()).isEqualTo(endDate.toString());
        assertThat(result.get(0).getReservationCount()).isEqualTo(25);
    }

}
