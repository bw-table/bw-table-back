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
    private String restaurant;
    private String name;
//    private String menus;
    private MultipartFile[] images;
    private List<MultipartFile> menuImages;
}
