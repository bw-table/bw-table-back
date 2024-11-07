package com.zero.bwtableback.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AnnouncementReqDto {

    private String title;
    private String content;
    private boolean event;
    private Long restaurantId;
}
