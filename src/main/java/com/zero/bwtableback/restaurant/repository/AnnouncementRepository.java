package com.zero.bwtableback.restaurant.repository;

import com.zero.bwtableback.restaurant.entity.Announcement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    Page<Announcement> findByRestaurantId(Long restaurantId, Pageable pageable);

    List<Announcement> findByEventTrue();
}
