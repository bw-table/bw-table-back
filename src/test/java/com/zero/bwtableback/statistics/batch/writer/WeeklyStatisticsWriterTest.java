package com.zero.bwtableback.statistics.batch.writer;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.zero.bwtableback.statistics.entity.Statistics;
import com.zero.bwtableback.statistics.entity.StatisticsType;
import com.zero.bwtableback.statistics.repository.StatisticsRepository;
import com.zero.bwtableback.statistics.util.DateRangeCalculator;
import com.zero.bwtableback.statistics.util.TimeKeyGenerator;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.Chunk;

@ExtendWith(MockitoExtension.class)
class WeeklyStatisticsWriterTest {

    @Mock
    private StatisticsRepository statisticsRepository;

    @InjectMocks
    private WeeklyStatisticsWriter weeklyStatisticsWriter;

    @DisplayName("주별 데이터를 저장하고 오래된 데이터를 삭제한다")
    @Test
    void givenWeeklyStatisticsData_whenWriting_thenDeletesOldAndSavesNew() {
        // Given
        LocalDate cutoffDate = LocalDate.now().minusWeeks(13);
        String cutoffDateKey = TimeKeyGenerator.generateWeeklyKey(
                DateRangeCalculator.getStartOfWeek(cutoffDate, 0),
                DateRangeCalculator.getEndDateOfWeek(cutoffDate)
        );

        List<Statistics> mockStatistics = mock(List.class);
        Chunk<List<Statistics>> chunk = new Chunk<>(List.of(mockStatistics));

        // When
        weeklyStatisticsWriter.write(chunk);

        // Then
        verify(statisticsRepository).bulkDeleteByRestaurantIdsAndTypeAndTimeKeyBefore(
                anyList(), eq(StatisticsType.WEEKLY), eq(cutoffDateKey)
        );
        verify(statisticsRepository).saveAll(anyList());
    }

}
