package com.zero.bwtableback.restaurant.repository;

import com.zero.bwtableback.restaurant.entity.TimeslotSetting;
import com.zero.bwtableback.restaurant.entity.WeekdaySetting;
import org.joda.time.DateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TimeslotSettingRepository extends JpaRepository<TimeslotSetting, Long> {

    List<TimeslotSetting> findByWeekdaySetting(WeekdaySetting weekdaySetting);
    void deleteByWeekdaySettingIn(List<WeekdaySetting> weekdaySettings);

    Optional<TimeslotSetting> findByWeekdaySettingAndTimeslot(WeekdaySetting weekdaySetting, LocalTime timeslot);
}
