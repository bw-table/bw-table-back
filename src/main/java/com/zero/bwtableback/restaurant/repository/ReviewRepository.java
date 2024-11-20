package com.zero.bwtableback.restaurant.repository;

import com.zero.bwtableback.restaurant.entity.Restaurant;
import com.zero.bwtableback.restaurant.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByRestaurant_Id(Long id, Pageable pageable);
    // 가게 아이디 기준 정렬
    Page<Review> findByMemberIdOrderByRestaurantId(Long memberId, Pageable pageable);

    List<Review> findByRestaurant(Restaurant restaurant);
}
