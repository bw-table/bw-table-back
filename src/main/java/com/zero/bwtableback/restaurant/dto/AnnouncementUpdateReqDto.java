package com.zero.bwtableback.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AnnouncementUpdateReqDto {

    private String title;
    private String content;
    private Boolean event;
}
