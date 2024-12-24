package com.zero.bwtableback.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RestaurantRegistrationDto {
    private String name;
    private String description;
    private String address;
    private double latitude;
    private double longitude;
    private String contact;
    private String closedDay;
    private String category;
    private String info;
    private String link;
    private int deposit;

    private List<MenuRegisterDto> menus;
    private List<OperatingHoursDto> operatingHours;
    private List<String> facilities;
    private List<String> hashtags;

    private List<MultipartFile> images;
    private List<MultipartFile> menuImages;
//    private String menus;
//    private MultipartFile[] images;
//    private List<MultipartFile> menuImages;
}
