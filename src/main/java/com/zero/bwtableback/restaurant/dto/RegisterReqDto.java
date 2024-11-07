package com.zero.bwtableback.restaurant.dto;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
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

    @NotNull(message = "Latitude must not be null")
    private double latitude;

    @NotNull(message = "Longitude must not be null")
    private double longitude;

    @NotNull(message = "Contact must not be null")
    private String contact;

    private String closedDay;

    @NotNull(message = "Category must not be null")
    private String category;

    private String info;

    private String link;

    private int deposit;

    @NotNull(message = "Menu must not be null")
    private List<MenuDto> menus;

    @NotNull(message = "OperatingHours must not be null")
    private List<OperatingHoursDto> operatingHours;

    @NotNull(message = "Images must not be null")
    private List<String> images;

    private List<String> facilities;

    private List<String> hashtags;

    // null을 허용하는 편의시설과 해시태그 필드 null 체크 및 초기화
    public void validate() {
        if (facilities == null) {
            facilities = new ArrayList<>();
        }

        if (hashtags == null) {
            hashtags = new ArrayList<>();
        }
    }
}
