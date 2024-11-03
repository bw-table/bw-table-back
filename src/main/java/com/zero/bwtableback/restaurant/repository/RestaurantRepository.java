package com.zero.bwtableback.restaurant.repository;

import com.zero.bwtableback.restaurant.entity.CategoryType;
import com.zero.bwtableback.restaurant.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    boolean existsByAddress(String address);
    boolean existsByContact(String contact);
    List<Restaurant> findByNameContainingIgnoreCase(String name);
    List<Restaurant> findByCategory_CategoryType(CategoryType type);
    List<Restaurant> findByHashtags_NameContaining(String hashtag);
}
