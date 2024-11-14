package com.zero.bwtableback.restaurant.repository;

import com.zero.bwtableback.restaurant.entity.ReservationSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservationSettingRepository extends JpaRepository<ReservationSetting, Long> {

    boolean existsByRestaurantIdAndStartDateBeforeAndEndDateAfter(
            Long restaurantId, LocalDate startDate, LocalDate endDate);

    List<ReservationSetting> findByRestaurantId(Long restaurantId);
}
