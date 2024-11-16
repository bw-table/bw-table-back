package com.zero.bwtableback.restaurant.repository;

import com.zero.bwtableback.restaurant.dto.RestaurantListDto;
import com.zero.bwtableback.restaurant.entity.Facility;
import com.zero.bwtableback.restaurant.entity.FacilityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FacilityRepository extends JpaRepository<Facility, Long> {

    Optional<Facility> findByFacilityType(FacilityType type);

    List<RestaurantListDto> findRestaurantsByFacilityType(FacilityType facilityType);
}
