package com.zero.bwtableback.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RestaurantListDto {

    private Long id;
    private String name;
    private String address;
    private String category;
}
