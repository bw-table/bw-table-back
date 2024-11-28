package com.zero.bwtableback.restaurant.repository;

import com.zero.bwtableback.restaurant.entity.ReservationCapacity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

@Repository
public interface ReservationCapacityRepository extends JpaRepository<ReservationCapacity, Long> {
    Optional<ReservationCapacity> findByRestaurantIdAndDateAndTimeslot(Long restaurantId, LocalDate date, LocalTime timeslot);
}
