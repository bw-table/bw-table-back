package com.zero.bwtableback.member.dto;

import com.zero.bwtableback.restaurant.entity.Category;
import com.zero.bwtableback.restaurant.entity.CategoryType;
import com.zero.bwtableback.restaurant.entity.Review;
import com.zero.bwtableback.restaurant.entity.ReviewImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MyReviewResDto {
    private Long id;
    private String content;
    private int rating;
    private List<String> images;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long restaurantId;
    private String restaurantName;
    private CategoryType restaurantCategory;
    private Long memberId;
    private String memberProfileImage;
    private String memberNickname;

    public static MyReviewResDto fromEntity(Review review) {
        return MyReviewResDto.builder()
                .id(review.getId())
                .content(review.getContent())
                .rating(review.getRating())
                .images(review.getImages().stream()
                        .map(ReviewImage::getImageUrl)
                        .collect(Collectors.toList()))
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .restaurantId(review.getRestaurant().getId())
                .restaurantName(review.getRestaurant().getName())
                .restaurantCategory(review.getRestaurant().getCategory().getCategoryType())
                .memberId(review.getMember().getId())
                .memberProfileImage(review.getMember().getProfileImage())
                .memberNickname(review.getMember().getNickname())
                .build();
    }
}
