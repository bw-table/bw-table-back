package com.zero.bwtableback.restaurant.service;

import com.zero.bwtableback.restaurant.dto.RestaurantReqDto;
import com.zero.bwtableback.restaurant.entity.*;
import com.zero.bwtableback.restaurant.repository.CategoryRepository;
import com.zero.bwtableback.restaurant.repository.FacilityRepository;
import com.zero.bwtableback.restaurant.repository.HashtagRepository;
import com.zero.bwtableback.restaurant.repository.RestaurantRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final CategoryRepository categoryRepository;
    private final FacilityRepository facilityRepository;
    private final HashtagRepository hashtagRepository;

    @Autowired
    public RestaurantServiceImpl(RestaurantRepository restaurantRepository,
                                 CategoryRepository categoryRepository,
                                 FacilityRepository facilityRepository,
                                 HashtagRepository hashtagRepository) {
        this.restaurantRepository = restaurantRepository;
        this.categoryRepository = categoryRepository;
        this.facilityRepository = facilityRepository;
        this.hashtagRepository = hashtagRepository;
    }

    /**
     * 가게 등록
     */
    @Override
    @Transactional
    public Restaurant registerRestaurant(RestaurantReqDto reqDto) {

        Restaurant restaurant = Restaurant.builder()
                .name(reqDto.getName())
                .description(reqDto.getDescription())
                .address(reqDto.getAddress())
                .contact(reqDto.getContact())
                .closedDay(reqDto.getClosedDay())
                .build();

        // 카테고리 설정
        if (reqDto.getCategoryId() != null) {
            Category category = categoryRepository.findById(reqDto.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found"));
            restaurant.setCategory(category);
        }

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

        // 메뉴 추가
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

        // 이미지 추가
        List<RestaurantImage> images = reqDto.getImages().stream()
                .map(image -> RestaurantImage.builder()
                        .imageUrl(image)
                        .restaurant(restaurant)
                        .build())
                .collect(Collectors.toList());
        restaurant.setImages(new HashSet<>(images));

        // 편의시설 추가
        List<Facility> facilities = reqDto.getFacilities().stream()
                .map(facilityId -> facilityRepository.findById(facilityId)
                        .orElseThrow(() -> new EntityNotFoundException("Facility not found")))
                .collect(Collectors.toList());
        restaurant.setFacilities(facilities);

        // 해시태그 추가
        List<Hashtag> hashtags = reqDto.getHashtags().stream()
                .map(hashtagId -> hashtagRepository.findById(hashtagId)
                        .orElseThrow(() -> new EntityNotFoundException("Hashtag not found")))
                .collect(Collectors.toList());
        restaurant.setHashtags(hashtags);

        return restaurantRepository.save(restaurant);
    }
}
