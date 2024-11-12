package com.zero.bwtableback.restaurant.repository;

import com.zero.bwtableback.restaurant.entity.TimeslotSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TimeslotSettingRepository extends JpaRepository<TimeslotSetting, Long> {
}
