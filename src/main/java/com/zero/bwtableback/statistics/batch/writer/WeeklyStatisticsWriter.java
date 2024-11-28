package com.zero.bwtableback.statistics.batch.writer;

import com.zero.bwtableback.statistics.entity.Statistics;
import com.zero.bwtableback.statistics.entity.StatisticsType;
import com.zero.bwtableback.statistics.repository.StatisticsRepository;
import com.zero.bwtableback.statistics.util.DateRangeCalculator;
import com.zero.bwtableback.statistics.util.TimeKeyGenerator;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class WeeklyStatisticsWriter implements ItemWriter<List<Statistics>> {

    private final StatisticsRepository statisticsRepository;

    @Override
    public void write(@NotNull Chunk<? extends List<Statistics>> items) {
        try {
            // 13주 전 데이터 삭제
            LocalDate cutoffDate = LocalDate.now().minusWeeks(13);
            LocalDate startOfCutoffWeek = DateRangeCalculator.getStartOfWeek(cutoffDate, 0);
            LocalDate endOfCutoffWeek = DateRangeCalculator.getEndDateOfWeek(startOfCutoffWeek);

            List<Long> restaurantIds = items.getItems().stream()
                    .flatMap(List::stream)
                    .map(statistics -> statistics.getRestaurant().getId())
                    .distinct()
                    .toList();

            statisticsRepository.bulkDeleteByRestaurantIdsAndTypeAndTimeKeyBefore(
                    restaurantIds,
                    StatisticsType.WEEKLY,
                    TimeKeyGenerator.generateWeeklyKey(startOfCutoffWeek, endOfCutoffWeek)
            );

            // 새로운 데이터 저장
            List<Statistics> allStatistics = items.getItems().stream()
                    .flatMap(List::stream)
                    .toList();
            statisticsRepository.saveAll(allStatistics);

            log.info("주별 통계 저장 성공: {} 개", allStatistics.size());
        } catch (Exception e) {
            log.error("주별 통계 처리 실패", e);
        }
    }

}
