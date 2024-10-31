package com.zero.bwtableback.restaurant.repository;

import com.zero.bwtableback.restaurant.entity.Facility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FacilityRepository extends JpaRepository<Facility, Long> {

}
