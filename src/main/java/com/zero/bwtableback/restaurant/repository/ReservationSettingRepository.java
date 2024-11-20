package com.zero.bwtableback.restaurant.repository;

import com.zero.bwtableback.restaurant.entity.ReservationSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationSettingRepository extends JpaRepository<ReservationSetting, Long> {

    @Query("SELECT COUNT(rs) > 0 FROM ReservationSetting rs " +
            "WHERE rs.restaurantId = :restaurantId " +
            "AND ((rs.startDate BETWEEN :startDate AND :endDate) " +
            "      OR (rs.endDate BETWEEN :startDate AND :endDate) " +
            "      OR (rs.startDate <= :startDate AND rs.endDate >= :endDate))")
    boolean existsByRestaurantIdAndOverlappingDates(
            @Param("restaurantId") Long restaurantId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    List<ReservationSetting> findByRestaurantId(Long restaurantId);

    @Query("SELECT rs FROM ReservationSetting rs WHERE rs.restaurantId = :restaurantId " +
            "AND :reservationDate BETWEEN rs.startDate AND rs.endDate")
    Optional<ReservationSetting> findByRestaurantIdAndDateRange(@Param("restaurantId") Long restaurantId,
                                                                @Param("reservationDate") LocalDate reservationDate);
}
