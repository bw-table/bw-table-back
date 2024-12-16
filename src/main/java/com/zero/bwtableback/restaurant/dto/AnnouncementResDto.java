package com.zero.bwtableback.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
public class AnnouncementResDto {

    private Long id;
    private String message;
    private Long restaurantId;
}
