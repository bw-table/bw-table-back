package com.zero.bwtableback.restaurant.service;

import com.zero.bwtableback.restaurant.dto.ReviewInfoDto;
import com.zero.bwtableback.restaurant.entity.Restaurant;
import com.zero.bwtableback.restaurant.repository.RestaurantRepository;
import com.zero.bwtableback.restaurant.dto.ReviewReqDto;
import com.zero.bwtableback.restaurant.dto.ReviewResDto;
import com.zero.bwtableback.restaurant.entity.Review;
import com.zero.bwtableback.restaurant.repository.ReviewRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final RestaurantRepository restaurantRepository;

    // 리뷰 작성
    public ReviewResDto createReview(Long restaurantId, ReviewReqDto reqDto) {

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found with id: " + restaurantId));

        Review review = Review.builder()
                .content(reqDto.getContent())
                .rating(reqDto.getRating())
                .restaurant(restaurant)
                .build();

        Review savedReview = reviewRepository.save(review);

        return ReviewResDto.builder()
                .id(savedReview.getId())
                .restaurantId(savedReview.getRestaurant().getId())
                .message("Review and rating added successfully")
                .build();
    }

    // 식당 리뷰 목록 조회
    public List<ReviewInfoDto> getReviewsByRestaurant(Long restaurantId, Pageable pageable) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found with id: " + restaurantId));

        Page<Review> reviews = reviewRepository.findByRestaurant_Id(restaurantId, pageable);

        return reviews.stream()
                .map(this::convertToInfoDto)
                .collect(Collectors.toList());
    }

    // 리뷰 상세 조회
    public ReviewInfoDto getReviewById(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Review not found with id: " + id));

        return convertToInfoDto(review);
    }

    private ReviewInfoDto convertToInfoDto(Review review) {
        return ReviewInfoDto.builder()
                .id(review.getId())
                .content(review.getContent())
                .rating(review.getRating())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .restaurantId(review.getRestaurant().getId())
                .build();
    }
}
