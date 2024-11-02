package com.zero.bwtableback.restaurant.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RestaurantInfoDto {

    private Long id;
    private String name;
    private String description;
    private String address;
    private String contact;
    private String closedDay;
    private String category;
    private String link;
    private String notice;
    private List<String> images;
    private List<MenuDto> menus;
    private List<String> facilities;
    private List<String> hashtags;
    private List<OperatingHoursDto> operatingHours;
}
