package com.zero.bwtableback.restaurant.repository;

import com.zero.bwtableback.restaurant.entity.ReservationSetting;
import com.zero.bwtableback.restaurant.entity.WeekdaySetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WeekdaySettingRepository extends JpaRepository<WeekdaySetting, Long> {

    List<WeekdaySetting> findByReservationSetting(ReservationSetting reservationSetting);
    void deleteByReservationSetting(ReservationSetting reservationSetting);
}
