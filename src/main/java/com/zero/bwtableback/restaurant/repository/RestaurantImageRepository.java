package com.zero.bwtableback.restaurant.repository;

import com.zero.bwtableback.restaurant.entity.RestaurantImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantImageRepository extends JpaRepository<RestaurantImage, Long> {
}
