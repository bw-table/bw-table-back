package com.zero.bwtableback.restaurant.service;

import com.zero.bwtableback.restaurant.dto.RestaurantListDto;
import com.zero.bwtableback.restaurant.entity.*;
import com.zero.bwtableback.restaurant.repository.CategoryRepository;
import com.zero.bwtableback.restaurant.repository.HashtagRepository;
import com.zero.bwtableback.restaurant.repository.RestaurantRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RestaurantSearchService {

    private final RestaurantRepository restaurantRepository;
    private final CategoryRepository categoryRepository;
    private final HashtagRepository hashtagRepository;

    // 모든 식당 리스트 검색
    public List<RestaurantListDto> getRestaurants(Pageable pageable) {
        Page<Restaurant> restaurants = restaurantRepository.findAll(pageable);

        return restaurants.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 이름으로 식당 검색
    public List<RestaurantListDto> getRestaurantsByName(String name, Pageable pageable) {
        Page<Restaurant> restaurants = restaurantRepository.findByNameContainingIgnoreCase(name, pageable);

        return restaurants.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 업종으로 식당 검색
    public List<RestaurantListDto> getRestaurantsByCategory(String category, Pageable pageable) {
        CategoryType categoryType = convertToCategoryType(category);

        Optional<Category> optionalCategory = categoryRepository.findByCategoryType(categoryType);

        optionalCategory.ifPresent(categoryEntity -> {
            categoryEntity.setSearchCount(categoryEntity.getSearchCount() + 1);
            categoryRepository.save(categoryEntity);
        });

        Category categoryEntity = optionalCategory.orElseThrow(() ->
                new EntityNotFoundException("Category not found"));

        Page<Restaurant> restaurants = restaurantRepository.findByCategory_CategoryType(categoryType, pageable);

        return restaurants.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 메뉴로 식당 검색
    public List<RestaurantListDto> getRestaurantsByMenu(String menu, Pageable pageable) {
        Page<Restaurant> restaurants = restaurantRepository.findByMenus_NameContaining(menu, pageable);

        return restaurants.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 해시태그로 식당 검색
    public List<RestaurantListDto> getRestaurantsByHashtag(String hashtag, Pageable pageable) {
        Optional<Hashtag> optionalHashtag = hashtagRepository.findByName(hashtag);

        optionalHashtag.ifPresent(hashtagEntity -> {
            hashtagEntity.setSearchCount(hashtagEntity.getSearchCount() + 1);
            hashtagRepository.save(hashtagEntity);
        });

//        Hashtag hashtagEntity = optionalHashtag.orElseThrow(() -> (
//            new EntityNotFoundException("Hashtag not found")));

        Page<Restaurant> restaurants = restaurantRepository.findByHashtags_Name(hashtag, pageable);

        return restaurants.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 해시태그 자동완성
    public List<String> getHashtagSuggestions(String hashtag) {
        List<Hashtag> hashtags = hashtagRepository.findTop10ByNameStartingWithIgnoreCase(hashtag);

        return hashtags.stream()
                .map(Hashtag::getName)
                .collect(Collectors.toList());
    }

    // Restaurant -> dto로 변환하는 헬퍼 메서드
    private RestaurantListDto convertToDto(Restaurant restaurant) {
        String firstImageUrl = restaurant.getImages().stream()
                .findFirst()
                .map(RestaurantImage::getImageUrl)
                .orElse(null);

        return new RestaurantListDto(
                restaurant.getId(),
                restaurant.getName(),
                restaurant.getAddress(),
                restaurant.getCategory() != null ? restaurant.getCategory().getCategoryType().name() : null,
                restaurant.getAverageRating(),
                firstImageUrl
        );
    }

    // category String -> categoryType enum 으로 변환하는 헬퍼 메서드
    private CategoryType convertToCategoryType(String category) {
        if (category == null) {
            throw new IllegalArgumentException("Category must not be null");
        }

        try {
            return CategoryType.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid category type: " + category);
        }
    }

    // 특정 편의시설을 가진 식당 조회
    public List<Restaurant> getRestaurantsByFacility(FacilityType facilityType, Pageable pageable) {
        return restaurantRepository.findByFacilities_FacilityType(facilityType, pageable);
    }
}
