package com.zero.bwtableback.statistics.service;

import com.zero.bwtableback.statistics.dto.StatisticsDto;
import com.zero.bwtableback.statistics.entity.Statistics;
import com.zero.bwtableback.statistics.entity.StatisticsType;
import com.zero.bwtableback.statistics.repository.StatisticsRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class StatisticsService {

    private final StatisticsRepository statisticsRepository;

    // 일별 예약 건수 조회
    public List<StatisticsDto> getDailyReservationsLast30Days(Long restaurantId) {
        List<Statistics> statistics = statisticsRepository.findByRestaurantIdAndType(
                restaurantId,
                StatisticsType.DAILY);
        return StatisticsDto.fromEntities(statistics);
    }

    // 주별 예약 건수 조회
    public List<StatisticsDto> getWeeklyReservationsLast12Weeks(Long restaurantId) {
        List<Statistics> statistics = statisticsRepository.findByRestaurantIdAndType(
                restaurantId,
                StatisticsType.WEEKLY
        );
        return StatisticsDto.fromEntities(statistics);
    }

    // 월별 예약 건수 조회
    public List<StatisticsDto> getMonthlyReservationsLast6Months(Long restaurantId) {
        List<Statistics> statistics = statisticsRepository.findByRestaurantIdAndType(
                restaurantId,
                StatisticsType.MONTHLY
        );
        return StatisticsDto.fromEntities(statistics);
    }

    // 인기 시간대 조회
    public List<StatisticsDto> getPopularTimesLast30Days(Long restaurantId) {
        List<Statistics> statistics = statisticsRepository.findByRestaurantIdAndType(
                restaurantId,
                StatisticsType.TIME_SLOT
        );
        return StatisticsDto.fromEntities(statistics);
    }

    // 인기 일자 조회
    public List<StatisticsDto> getTop5PopularDatesLast30Days(Long restaurantId) {
        List<Statistics> statistics = statisticsRepository.findByRestaurantIdAndType(
                restaurantId,
                StatisticsType.POPULAR_DATE
        );
        return StatisticsDto.fromEntities(statistics);
    }

}
