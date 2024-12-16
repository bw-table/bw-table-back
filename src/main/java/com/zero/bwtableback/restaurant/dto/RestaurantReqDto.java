package com.zero.bwtableback.restaurant.dto;


import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RestaurantReqDto {

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

    @NotNull(message = "Deposit must not be null")
    private int deposit;

    @NotNull(message = "Menu must not be null")
    private List<MenuRegisterDto> menus;

    @NotNull(message = "OperatingHours must not be null")
    private List<OperatingHoursDto> operatingHours;

    @NotNull(message = "Images must not be null")
    private MultipartFile[] images;

    private List<String> facilities;

    private List<String> hashtags;

    // null을 허용하는 편의시설과 해시태그 필드 null 체크 및 초기화
    // TODO: 입력하지 않을 경우 어떤 식으로 응답 들어오는지 확인 후 삭제
    public void validate() {
        if (facilities == null) {
            facilities = new ArrayList<>();
        }

        if (hashtags == null) {
            hashtags = new ArrayList<>();
        }
    }
}
