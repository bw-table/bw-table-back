package com.zero.bwtableback.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MenuDto {

//    private Long id;
    private String name;
    private int price;
    private String description;
    private String imageUrl;
}
