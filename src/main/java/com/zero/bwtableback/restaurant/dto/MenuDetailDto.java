package com.zero.bwtableback.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MenuDetailDto { // 식당 상세 조회 시 사용

    private Long id;
    private String name;
    private int price;
    private String description;
    private String imageUrl;
    private Long restaurantId;
}
