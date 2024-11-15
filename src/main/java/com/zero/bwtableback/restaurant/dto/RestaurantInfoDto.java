package com.zero.bwtableback.restaurant.dto;


import com.zero.bwtableback.restaurant.entity.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * 식당 상세정보 조회 시 응답
 */
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RestaurantInfoDto {

    private Long id;
    private String name;
    private String description;
    private String address;
    private double latitude;
    private double longitude;
    private String contact;
    private String closedDay;
    private String category;
    private String link;
    private String info;
    private int deposit;
    private List<String> images;
    private List<MenuDto> menus;
    private List<String> facilities;
    private List<String> hashtags;
    private List<OperatingHoursDto> operatingHours;
    private double averageRating;
    private List<ReviewInfoDto> reviews;
    private List<AnnouncementDetailDto> announcements;
}
