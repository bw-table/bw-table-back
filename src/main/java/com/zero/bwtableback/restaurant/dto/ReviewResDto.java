package com.zero.bwtableback.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ReviewResDto {

    private Long id;
    private Long restaurantId;
    private String message;
}
