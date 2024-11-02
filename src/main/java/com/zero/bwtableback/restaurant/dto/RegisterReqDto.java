package com.zero.bwtableback.restaurant.dto;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class RegisterReqDto {

    @NotNull(message = "Name must not be null")
    private String name;

    private String description;

    @NotNull(message = "Address must not be null")
    private String address;

    @NotNull(message = "Contact must not be null")
    private String contact;

    private String closedDay;

    @NotNull(message = "Category must not be null")
    private String category;

    private String notice;

    private String link;

    @NotNull(message = "Menu must not be null")
    private List<MenuDto> menus;

    @NotNull(message = "OperatingHours must not be null")
    private List<OperatingHoursDto> operatingHours;

    private List<String> images;

    private List<String> facilities;

    private List<String> hashtags;
}
