package com.zero.bwtableback.statistics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.zero.bwtableback.restaurant.repository.RestaurantRepository;
import com.zero.bwtableback.statistics.dto.StatisticsDto;
import com.zero.bwtableback.statistics.entity.Statistics;
import com.zero.bwtableback.statistics.entity.StatisticsType;
import com.zero.bwtableback.statistics.repository.StatisticsRepository;
import com.zero.bwtableback.statistics.service.StatisticsService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

    @Mock
    private StatisticsRepository statisticsRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @InjectMocks
    private StatisticsService statisticsService;

    @DisplayName("일별 예약 건수 - 최근 30일 데이터 반환")
    @Test
    void shouldReturnDailyReservationsLast30Days() {
        // given
        Long restaurantId = 1L;
        Long memberId = 1L;
        List<Statistics> mockStatistics = List.of(
                new Statistics(1L, null, StatisticsType.DAILY, "2024-11-01", 10),
                new Statistics(2L, null, StatisticsType.DAILY, "2024-11-02", 8)
        );
        given(restaurantRepository.findRestaurantIdByMemberId(memberId)).willReturn(restaurantId);
        given(statisticsRepository.findByRestaurantIdAndType(restaurantId, StatisticsType.DAILY)).willReturn(mockStatistics);

        // when
        List<StatisticsDto> result = statisticsService.getDailyReservationsLast30Days(restaurantId, memberId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).periodKey()).isEqualTo("2024-11-01");
        assertThat(result.get(0).reservationCount()).isEqualTo(10);
        assertThat(result.get(1).periodKey()).isEqualTo("2024-11-02");
        assertThat(result.get(1).reservationCount()).isEqualTo(8);
    }

    @DisplayName("주별 예약 건수 - 최근 12주 데이터 반환")
    @Test
    void shouldReturnWeeklyReservationsLast12Weeks() {
        // given
        Long restaurantId = 1L;
        Long memberId = 1L;
        List<Statistics> mockStatistics = List.of(
                new Statistics(1L, null, StatisticsType.WEEKLY, "2024-W44", 50),
                new Statistics(2L, null, StatisticsType.WEEKLY, "2024-W45", 65)
        );
        given(restaurantRepository.findRestaurantIdByMemberId(memberId)).willReturn(restaurantId);
        given(statisticsRepository.findByRestaurantIdAndType(restaurantId, StatisticsType.WEEKLY)).willReturn(mockStatistics);

        // when
        List<StatisticsDto> result = statisticsService.getWeeklyReservationsLast12Weeks(restaurantId, memberId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).periodKey()).isEqualTo("2024-W44");
        assertThat(result.get(0).reservationCount()).isEqualTo(50);
        assertThat(result.get(1).periodKey()).isEqualTo("2024-W45");
        assertThat(result.get(1).reservationCount()).isEqualTo(65);
    }

    @DisplayName("월별 예약 건수 - 최근 6개월 데이터 반환")
    @Test
    void shouldReturnMonthlyReservationsLast6Months() {
        // given
        Long restaurantId = 1L;
        Long memberId = 1L;
        List<Statistics> mockStatistics = List.of(
                new Statistics(1L, null, StatisticsType.MONTHLY, "2024-06", 300),
                new Statistics(2L, null, StatisticsType.MONTHLY, "2024-07", 320)
        );
        given(restaurantRepository.findRestaurantIdByMemberId(memberId)).willReturn(restaurantId);
        given(statisticsRepository.findByRestaurantIdAndType(restaurantId, StatisticsType.MONTHLY)).willReturn(mockStatistics);

        // when
        List<StatisticsDto> result = statisticsService.getMonthlyReservationsLast6Months(restaurantId, memberId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).periodKey()).isEqualTo("2024-06");
        assertThat(result.get(0).reservationCount()).isEqualTo(300);
        assertThat(result.get(1).periodKey()).isEqualTo("2024-07");
        assertThat(result.get(1).reservationCount()).isEqualTo(320);
    }

    @DisplayName("인기 예약 시간대 - 최근 30일 데이터 반환")
    @Test
    void shouldReturnPopularTimesLast30Days() {
        // given
        Long restaurantId = 1L;
        Long memberId = 1L;
        List<Statistics> mockStatistics = List.of(
                new Statistics(1L, null, StatisticsType.TIME_SLOT, "18:00", 80),
                new Statistics(2L, null, StatisticsType.TIME_SLOT, "19:00", 75)
        );
        given(restaurantRepository.findRestaurantIdByMemberId(memberId)).willReturn(restaurantId);
        given(statisticsRepository.findByRestaurantIdAndType(restaurantId, StatisticsType.TIME_SLOT)).willReturn(mockStatistics);

        // when
        List<StatisticsDto> result = statisticsService.getPopularTimesLast30Days(restaurantId, memberId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).periodKey()).isEqualTo("18:00");
        assertThat(result.get(0).reservationCount()).isEqualTo(80);
        assertThat(result.get(1).periodKey()).isEqualTo("19:00");
        assertThat(result.get(1).reservationCount()).isEqualTo(75);
    }

    @DisplayName("인기 예약 일자 - 최근 30일 TOP 5 데이터 반환")
    @Test
    void shouldReturnTop5PopularDatesLast30Days() {
        // given
        Long restaurantId = 1L;
        Long memberId = 1L;
        List<Statistics> mockStatistics = List.of(
                new Statistics(1L, null, StatisticsType.POPULAR_DATE, "2024-11-11", 25),
                new Statistics(2L, null, StatisticsType.POPULAR_DATE, "2024-11-15", 20)
        );
        given(restaurantRepository.findRestaurantIdByMemberId(memberId)).willReturn(restaurantId);
        given(statisticsRepository.findByRestaurantIdAndType(restaurantId, StatisticsType.POPULAR_DATE)).willReturn(mockStatistics);

        // when
        List<StatisticsDto> result = statisticsService.getTop5PopularDatesLast30Days(restaurantId, memberId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).periodKey()).isEqualTo("2024-11-11");
        assertThat(result.get(0).reservationCount()).isEqualTo(25);
        assertThat(result.get(1).periodKey()).isEqualTo("2024-11-15");
        assertThat(result.get(1).reservationCount()).isEqualTo(20);
    }

}
