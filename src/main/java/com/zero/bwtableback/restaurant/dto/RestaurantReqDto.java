package com.zero.bwtableback.restaurant.dto;

import com.zero.bwtableback.restaurant.entity.OperatingHours;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RestaurantReqDto {

    private String name;
    private String description;
    private String address;
    private String contact;
    private String closedDay;
    private Long categoryId;
    private List<MenuDto> menus;
    private List<OperatingHoursDto> operatingHours;
    private List<String> images;
    private List<Long> facilities;
    private List<Long> hashtags;
}
