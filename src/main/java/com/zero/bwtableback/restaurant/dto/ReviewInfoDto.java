package com.zero.bwtableback.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

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
}
