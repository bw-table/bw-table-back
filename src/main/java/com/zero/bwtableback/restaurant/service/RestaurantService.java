package com.zero.bwtableback.restaurant.service;

import com.zero.bwtableback.restaurant.dto.*;
import com.zero.bwtableback.restaurant.entity.*;
import com.zero.bwtableback.restaurant.exception.RestaurantException;
import com.zero.bwtableback.restaurant.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final CategoryRepository categoryRepository;
    private final FacilityRepository facilityRepository;
    private final HashtagRepository hashtagRepository;
    private final RestaurantImageRepository restaurantImageRepository;

    // 등록
    @Transactional
    public Restaurant registerRestaurant(RegisterReqDto reqDto) {
        // 카테고리 설정
        Category category = null;
        if (reqDto.getCategory() != null) {
            try {
                CategoryType categoryType = CategoryType.valueOf(reqDto.getCategory().toUpperCase());
                category = categoryRepository.findByCategoryType(categoryType)
                        .orElseThrow(() -> new EntityNotFoundException("Category not found"));
            } catch (IllegalArgumentException e) {
                throw new RestaurantException("Invalid category type provided: " + reqDto.getCategory());
            }
        }

        // 주소, 연락처 중복 체크
        if (restaurantRepository.existsByAddress(reqDto.getAddress())) {
            throw new RestaurantException("Restaurant with this address already exists.");
        }
        if (restaurantRepository.existsByContact(reqDto.getContact())) {
            throw new RestaurantException("Restaurant with this contact number already exists.");
        }

        // 레스토랑 객체 생성
        Restaurant restaurant = Restaurant.builder()
                .name(reqDto.getName())
                .description(reqDto.getDescription())
                .address(reqDto.getAddress())
                .contact(reqDto.getContact())
                .closedDay(reqDto.getClosedDay())
                .link(reqDto.getLink())
                .notice(reqDto.getNotice())
                .category(category)
                .images(new HashSet<>())
                .operatingHours(new ArrayList<>())
                .menus(new ArrayList<>())
                .facilities(new ArrayList<>())
                .hashtags(new ArrayList<>())
                .build();

        // 영업시간 설정
        List<OperatingHours> operatingHours = reqDto.getOperatingHours().stream()
                .map(hoursDto -> OperatingHours.builder()
                        .dayOfWeek(hoursDto.getDayOfWeek())
                        .openingTime(hoursDto.getOpeningTime())
                        .closingTime(hoursDto.getClosingTime())
                        .restaurant(restaurant)
                        .build())
                .collect(Collectors.toList());
        restaurant.setOperatingHours(operatingHours);

        // 메뉴 설정
        List<Menu> menus = reqDto.getMenus().stream()
                .map(menuDto -> Menu.builder()
                        .name(menuDto.getName())
                        .price(menuDto.getPrice())
                        .description(menuDto.getDescription())
                        .imageUrl(menuDto.getImageUrl())
                        .restaurant(restaurant)
                        .build())
                .collect(Collectors.toList());
        restaurant.setMenus(menus);

        // 편의시설 설정
        List<Facility> facilities = reqDto.getFacilities().stream()
                .map(facilityType -> {
                    FacilityType type = FacilityType.valueOf(facilityType);
                    return facilityRepository.findByFacilityType(type)
                            .orElseThrow(() -> new EntityNotFoundException("Facility not found"));
                })
                .collect(Collectors.toList());
        restaurant.setFacilities(facilities);

        // 해시태그 설정
        List<Hashtag> hashtags = reqDto.getHashtags().stream()
                .map(tag -> hashtagRepository.findByName(tag)
                        .orElseGet(() -> {
                            Hashtag newHashtag = new Hashtag(tag);
                            return hashtagRepository.save(newHashtag);
                        }))
                .collect(Collectors.toList());
        restaurant.setHashtags(hashtags);

        // 레스토랑 저장
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);

        Set<RestaurantImage> images = reqDto.getImages().stream()
                .map(imageUrl -> RestaurantImage.builder()
                        .imageUrl(imageUrl)
                        .restaurant(savedRestaurant)
                        .build())
                .collect(Collectors.toSet());
        savedRestaurant.setImages(images);

        if (images.isEmpty()) {
            System.out.println("No images to save.");
        } else {
            restaurantImageRepository.saveAll(images);
        }

        return savedRestaurant;
    }

    // 모든 식당 리스트 검색
    public List<RestaurantListDto> getRestaurants() {
        List<Restaurant> restaurants = restaurantRepository.findAll();
        return restaurants.stream()
                .map(restaurant -> new RestaurantListDto(
                        restaurant.getId(),
                        restaurant.getName(),
                        restaurant.getAddress(),
                        restaurant.getCategory() != null ? restaurant.getCategory().getCategoryType().name() : null
                )).collect(Collectors.toList());
    }

    // 식당 상세정보 조회
    public RestaurantInfoDto getRestaurantById(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found"));

        // 카테고리
        String category = restaurant.getCategory() != null ?
                restaurant.getCategory().getCategoryType().name() : null;

        // 이미지
        List<String> images = restaurant.getImages().stream()
                .map(RestaurantImage::getImageUrl)
                .collect(Collectors.toList());

        // 메뉴
        List<MenuDto> menus = restaurant.getMenus().stream()
                .map(menu -> new MenuDto(
                                    menu.getId(),
                                    menu.getName(),
                                    menu.getPrice(),
                                    menu.getDescription(),
                                    menu.getImageUrl(),
                                    menu.getRestaurant().getId()))
                .collect(Collectors.toList());

        // 영업시간
        List<OperatingHoursDto> operatingHours = restaurant.getOperatingHours().stream()
                .map(hours -> new OperatingHoursDto(
                                    hours.getId(),
                                    hours.getDayOfWeek(),
                                    hours.getOpeningTime(),
                                    hours.getClosingTime(),
                                    hours.getRestaurant().getId()))
                .collect(Collectors.toList());

        // 편의시설
        List<String> facilities = restaurant.getFacilities().stream()
                .map(facility -> facility.getFacilityType().name())
                .collect(Collectors.toList());

        // 해시태그
        List<String> hashtags = restaurant.getHashtags().stream()
                .map(Hashtag::getName)
                .collect(Collectors.toList());

        return RestaurantInfoDto.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .description(restaurant.getDescription())
                .address(restaurant.getAddress())
                .contact(restaurant.getContact())
                .closedDay(restaurant.getClosedDay())
                .category(category)
                .link(restaurant.getLink())
                .notice(restaurant.getNotice())
                .images(images)
                .menus(menus)
                .facilities(facilities)
                .hashtags(hashtags)
                .operatingHours(operatingHours)
                .build();
    }

}