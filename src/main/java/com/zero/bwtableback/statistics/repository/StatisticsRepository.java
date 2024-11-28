package com.zero.bwtableback.statistics.repository;

import com.zero.bwtableback.statistics.entity.Statistics;
import com.zero.bwtableback.statistics.entity.StatisticsType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface StatisticsRepository extends JpaRepository<Statistics, Long> {

    List<Statistics> findByRestaurantIdAndType(Long restaurantId, StatisticsType type);

    boolean existsByRestaurantIdAndType(Long restaurantId, StatisticsType type);

    @Transactional
    @Modifying
    @Query("""
    DELETE FROM Statistics s
    WHERE s.restaurant.id IN :restaurantIds
      AND s.type = :type
      AND s.timeKey < :timeKey
""")
    void bulkDeleteByRestaurantIdsAndTypeAndTimeKeyBefore(
            List<Long> restaurantIds,
            StatisticsType type,
            String timeKey
    );

    @Transactional
    @Modifying
    @Query("""
    DELETE FROM Statistics s
    WHERE s.restaurant.id IN :restaurantIds
      AND s.type = :type
""")
    void bulkDeleteByRestaurantIdsAndType(
            List<Long> restaurantIds,
            StatisticsType type
    );

}
