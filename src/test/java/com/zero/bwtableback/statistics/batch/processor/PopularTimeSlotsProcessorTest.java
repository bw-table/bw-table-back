package com.zero.bwtableback.statistics.batch.processor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.zero.bwtableback.reservation.repository.ReservationRepository;
import com.zero.bwtableback.restaurant.entity.Restaurant;
import com.zero.bwtableback.statistics.entity.Statistics;
import com.zero.bwtableback.statistics.entity.StatisticsType;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PopularTimeSlotsProcessorTest {

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private PopularTimeSlotsProcessor popularTimeSlotsProcessor;

    @DisplayName("인기 시간대 통계를 생성한다")
    @Test
    void givenReservationData_whenProcessing_thenGeneratePopularTimeSlotsStatistics() {
        // Given
        Restaurant restaurant = new Restaurant();
        restaurant.setId(1L);

        LocalDate today = LocalDate.now();
        LocalDate endDate = today.minusDays(1);
        LocalDate startDate = endDate.minusDays(30);

        List<Object[]> mockResults = List.of(
                new Object[]{"12:00", 15},
                new Object[]{"18:00", 25},
                new Object[]{"15:00", 10}
        );

        when(reservationRepository.aggregateTimeSlotStatistics(restaurant.getId(), startDate, endDate))
                .thenReturn(mockResults);

        // When
        List<Statistics> result = popularTimeSlotsProcessor.process(restaurant);

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).extracting(Statistics::getTimeKey)
                .containsExactly("18:00", "12:00", "15:00");
        assertThat(result).extracting(Statistics::getReservationCount)
                .containsExactly(25, 15, 10);
        assertThat(result).allMatch(statistics -> statistics.getType() == StatisticsType.TIME_SLOT);
        assertThat(result).allMatch(statistics -> statistics.getRestaurant().getId().equals(1L));
    }

}
