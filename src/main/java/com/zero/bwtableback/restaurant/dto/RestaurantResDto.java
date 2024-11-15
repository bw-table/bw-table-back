package com.zero.bwtableback.restaurant.dto;

import com.zero.bwtableback.restaurant.entity.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public class RestaurantResDto {

    private final Long restaurantId;
    private final String name;
    private final String description;
    private final String address; // address와 매핑
    private final String contact;
    private final String regularHoliday; // closedDay와 매핑
    private final String operatingHours; // 운영시간을 문자열로 변환하여 저장
    private final String info;
    private final List<String> images; // 이미지 URL 리스트
    private final String category; // category의 이름으로 변환
    private final List<String> menus; // 메뉴 리스트
    private final List<FacilityType> facilities; // 시설 리스트
    private final List<String> hashtags; // 해시태그 리스트

    // fromEntity 메서드
    public static RestaurantResDto fromEntity(Restaurant restaurant) {
        return new RestaurantResDto(
                restaurant.getId(),
                restaurant.getName(),
                restaurant.getDescription(),
                restaurant.getAddress(), // address를 location으로 사용
                restaurant.getContact(),
                restaurant.getClosedDay(), // closedDay를 regularHoliday로 사용
                restaurant.getOperatingHours().stream()
                        .map(operatingHour -> operatingHour.getOpeningTime() + "-" + operatingHour.getClosingTime()) // 운영시간을 문자열로 변환
                        .collect(Collectors.joining(", ")), // 예: "10:00-22:00"
                restaurant.getInfo(),
                restaurant.getImages().stream()
                        .map(image -> image.getImageUrl())
                        .collect(Collectors.toList()),
                restaurant.getCategory().getCategoryType().name(),
                restaurant.getMenus().stream()
                        .map(Menu::getName)
                        .collect(Collectors.toList()),
                restaurant.getFacilities().stream()
                        .map(Facility::getFacilityType)
                        .collect(Collectors.toList()),
                restaurant.getHashtags().stream()
                        .map(Hashtag::getName)
                        .collect(Collectors.toList())
        );
    }
}

