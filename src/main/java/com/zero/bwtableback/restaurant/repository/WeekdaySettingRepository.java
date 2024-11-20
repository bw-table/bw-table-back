package com.zero.bwtableback.restaurant.repository;

import com.zero.bwtableback.restaurant.entity.DayOfWeek;
import com.zero.bwtableback.restaurant.entity.ReservationSetting;
import com.zero.bwtableback.restaurant.entity.WeekdaySetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WeekdaySettingRepository extends JpaRepository<WeekdaySetting, Long> {

    List<WeekdaySetting> findByReservationSetting(ReservationSetting reservationSetting);
    void deleteByReservationSetting(ReservationSetting reservationSetting);

    @Query("SELECT w FROM WeekdaySetting w WHERE w.reservationSetting.id = :reservationSettingId AND w.dayOfWeek = :dayOfWeek")
    Optional<WeekdaySetting> findByReservationSettingIdAndDayOfWeek(@Param("reservationSettingId") Long reservationSettingId,
                                                    @Param("dayOfWeek") DayOfWeek dayOfWeek);
}
