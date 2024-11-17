package com.zero.bwtableback.restaurant.dto;

import lombok.Getter;

import java.util.List;

@Getter
//@AllArgsConstructor
public class UpdateReqDto {

    private String name;
    private String description;
    private String address;
    private Double latitude;
    private Double longitude;
    private String contact;
    private String closedDay;
    private String link;
    private String info;
    private Integer deposit;
    private String impCode;
    private String category;
    private List<OperatingHoursDto> operatingHours;
    private List<MenuUpdateDto> menus;
    private List<String> facilities;
    private List<String> hashtags;
//    private List<String> images;
    private List<Long> imageIdsToDelete;
}
