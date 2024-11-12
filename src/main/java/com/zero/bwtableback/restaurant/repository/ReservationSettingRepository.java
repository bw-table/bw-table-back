package com.zero.bwtableback.restaurant.repository;

import com.zero.bwtableback.restaurant.entity.ReservationSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationSettingRepository extends JpaRepository<ReservationSetting, Long> {
}
