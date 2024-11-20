package com.zero.bwtableback.restaurant.dto;

import com.zero.bwtableback.restaurant.entity.Review;
import com.zero.bwtableback.restaurant.entity.ReviewImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 리뷰 목록 조회 시 응답
 * 리뷰 상세 조회 시 응답
 */
@Builder
@Getter
@AllArgsConstructor
public class ReviewInfoDto {

    private Long id;
    private String content;
    private int rating;
    private List<String> images;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long restaurantId;

    private Long memberId;
    private String memberProfileImage;
    private String memberNickname;

    public static ReviewInfoDto fromEntity(Review review) {
        return ReviewInfoDto.builder()
                .id(review.getId())
                .content(review.getContent())
                .rating(review.getRating())
                .images(review.getImages().stream()
                        .map(ReviewImage::getImageUrl)
                        .collect(Collectors.toList()))
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .restaurantId(review.getRestaurant().getId())
                .memberId(review.getMember().getId())
                .memberProfileImage(review.getMember().getProfileImage())
                .memberNickname(review.getMember().getNickname())
                .build();
    }
}
