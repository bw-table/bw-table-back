package com.zero.bwtableback.statistics.batch.writer;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.zero.bwtableback.statistics.entity.Statistics;
import com.zero.bwtableback.statistics.entity.StatisticsType;
import com.zero.bwtableback.statistics.repository.StatisticsRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.Chunk;

@ExtendWith(MockitoExtension.class)
class PopularTimeSlotsWriterTest {

    @Mock
    private StatisticsRepository statisticsRepository;

    @InjectMocks
    private PopularTimeSlotsWriter popularTimeSlotsWriter;

    @DisplayName("인기 시간대 데이터를 저장하고 기존 데이터를 삭제한다")
    @Test
    void givenPopularTimeSlotsData_whenWriting_thenDeletesOldAndSavesNew() {
        // Given
        List<Statistics> mockStatistics = mock(List.class);
        Chunk<List<Statistics>> chunk = new Chunk<>(List.of(mockStatistics));

        // When
        popularTimeSlotsWriter.write(chunk);

        // Then
        verify(statisticsRepository).bulkDeleteByRestaurantIdsAndType(
                anyList(), eq(StatisticsType.TIME_SLOT)
        );
        verify(statisticsRepository).saveAll(anyList());
    }

}
