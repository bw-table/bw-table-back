package com.zero.bwtableback.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 식당 조회 시 응답
 */
@Getter
@AllArgsConstructor
public class RestaurantListDto {

    private Long id;
    private String name;
    private String address;
    private String category;
    private double averageRating;
}
