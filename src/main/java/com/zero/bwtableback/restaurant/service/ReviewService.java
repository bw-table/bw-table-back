package com.zero.bwtableback.restaurant.service;

import com.zero.bwtableback.restaurant.dto.ReviewInfoDto;
import com.zero.bwtableback.restaurant.entity.Restaurant;
import com.zero.bwtableback.restaurant.entity.ReviewImage;
import com.zero.bwtableback.restaurant.repository.RestaurantRepository;
import com.zero.bwtableback.restaurant.dto.ReviewReqDto;
import com.zero.bwtableback.restaurant.dto.ReviewResDto;
import com.zero.bwtableback.restaurant.entity.Review;
import com.zero.bwtableback.restaurant.repository.ReviewImageRepository;
import com.zero.bwtableback.restaurant.repository.ReviewRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final RestaurantRepository restaurantRepository;
    private final ReviewImageRepository reviewImageRepository;

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

        Set<ReviewImage> images = new HashSet<>();
        if (reqDto.getImages() != null && !reqDto.getImages().isEmpty()) {
            for (String imageUrl: reqDto.getImages()) {
                ReviewImage image = new ReviewImage(imageUrl, savedReview);
                images.add(image);
            }

            reviewImageRepository.saveAll(images);
        }

        ReviewResDto resDto = new ReviewResDto(
                savedReview.getId(),
                restaurantId,
                "Review and rating added successfully"
        );
        return resDto;
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
        List<String> images = review.getImages().stream()
                .map(ReviewImage::getImageUrl)
                .collect(Collectors.toList());

        return ReviewInfoDto.builder()
                .id(review.getId())
                .content(review.getContent())
                .rating(review.getRating())
                .images(images)
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .restaurantId(review.getRestaurant().getId())
                .build();
    }
}
