package com.zero.bwtableback.restaurant.repository;

import com.zero.bwtableback.restaurant.entity.CategoryType;
import com.zero.bwtableback.restaurant.entity.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    boolean existsByAddress(String address);
    boolean existsByContact(String contact);

    Page<Restaurant> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Restaurant> findByCategory_CategoryType(CategoryType type, Pageable pageable);
    Page<Restaurant> findByHashtags_NameContaining(String hashtag, Pageable pageable);
    Page<Restaurant> findByMenus_NameContaining(String menu, Pageable pageable);
}
