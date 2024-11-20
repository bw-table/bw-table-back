package com.zero.bwtableback.restaurant.repository;

import com.zero.bwtableback.restaurant.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {

    @Modifying
    @Transactional
    @Query("update Menu m set m.imageUrl = null where m.id = :menuId and m.restaurant.id = :restaurantId")
    void deleteMenuImageByRestaurantAndMenuId(@Param("restaurantId") Long restaurantId, @Param("menuId") Long menuId);
}
