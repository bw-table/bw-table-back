package com.zero.bwtableback.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MenuRegisterDto {

    private Long id;
    private String name;
    private int price;
    private String description;
    private MultipartFile image;
    private Long restaurantId;
}
