package com.zero.bwtableback.restaurant.repository;

import com.zero.bwtableback.restaurant.entity.WeekdaySetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WeekdaySettingRepository extends JpaRepository<WeekdaySetting, Long> {
}
