package com.zero.bwtableback.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RestaurantResDto {

    private Long id;
    private String name;
    private String description;
    private String address;
    private String contact;
    private String closedDay;
    private Long categoryId;
    private String categoryType;
    private List<MenuDto> menus;
    private List<OperatingHoursDto> operatingHours;
    private List<String> images;
    private List<Long> facilities;
    private List<Long> hashtags;
}
