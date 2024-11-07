package com.zero.bwtableback.restaurant.dto;

import lombok.Getter;

@Getter
public class AnnouncementReqDto {

    private String title;
    private String content;
    private boolean event;
    private Long restaurantId;
}
