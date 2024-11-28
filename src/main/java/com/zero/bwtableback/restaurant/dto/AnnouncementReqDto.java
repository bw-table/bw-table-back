package com.zero.bwtableback.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
public class AnnouncementReqDto {

    private String title;
    private String content;
    private boolean event;
    private Long restaurantId;
}
