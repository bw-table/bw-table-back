package com.zero.bwtableback.restaurant.service;

import com.zero.bwtableback.restaurant.entity.Restaurant;
import com.zero.bwtableback.restaurant.repository.RestaurantRepository;
import com.zero.bwtableback.restaurant.dto.ReviewReqDto;
import com.zero.bwtableback.restaurant.dto.ReviewResDto;
import com.zero.bwtableback.restaurant.entity.Review;
import com.zero.bwtableback.restaurant.repository.ReviewRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
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

        System.out.println("createReview 서비스 메서드 호출");

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found with id: " + restaurantId));

        Review review = Review.builder()
                .content(reqDto.getContent())
                .rating(reqDto.getRating())
                .restaurant(restaurant)
                .build();

        Review savedReview = reviewRepository.save(review);

        System.out.println("createReview 완료");

        return convertToResDto(savedReview);
    }

    // 식당 리뷰 목록 조회
    public List<ReviewResDto> getReviewsByRestaurant(Long restaurantId) {
        List<Review> reviews = reviewRepository.findByRestaurant_Id(restaurantId);

        return reviews.stream()
                .map(this::convertToResDto)
                .collect(Collectors.toList());
    }

    private ReviewResDto convertToResDto(Review review) {
        return ReviewResDto.builder()
                .id(review.getId())
                .content(review.getContent())
                .rating(review.getRating())
                .createdAt(review.getCreatedAt())
                .build();
    }

}
