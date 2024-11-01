package com.zero.bwtableback.restaurant.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterReqDto {

    private String name;
    private String description;
    private String address;
    private String contact;
    private String closedDay;
    private String category;
    private List<MenuDto> menus;
    private List<OperatingHoursDto> operatingHours;
    private List<String> images;
    private List<String> facilities;
    private List<String> hashtags;
}
