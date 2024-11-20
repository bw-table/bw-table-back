package com.zero.bwtableback.statistics.repository;

import com.zero.bwtableback.statistics.entity.Statistics;
import com.zero.bwtableback.statistics.entity.StatisticsType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatisticsRepository extends JpaRepository<Statistics, Long> {
    List<Statistics> findByRestaurantIdAndType(Long restaurantId, StatisticsType type);
}
