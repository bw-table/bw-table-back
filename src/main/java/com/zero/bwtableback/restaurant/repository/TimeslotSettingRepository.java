package com.zero.bwtableback.restaurant.repository;

import com.zero.bwtableback.restaurant.entity.TimeslotSetting;
import com.zero.bwtableback.restaurant.entity.WeekdaySetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimeslotSettingRepository extends JpaRepository<TimeslotSetting, Long> {

    List<TimeslotSetting> findByWeekdaySetting(WeekdaySetting weekdaySetting);
    void deleteByWeekdaySettingIn(List<WeekdaySetting> weekdaySettings);
}
