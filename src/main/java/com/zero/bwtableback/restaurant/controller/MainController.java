package com.zero.bwtableback.restaurant.controller;

import com.zero.bwtableback.restaurant.dto.RestaurantListDto;
import com.zero.bwtableback.restaurant.service.MainService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/main")
@RequiredArgsConstructor
public class MainController {

    private final MainService mainService;

    @GetMapping
    public ResponseEntity<Map<String, List<RestaurantListDto>>> getMainPageData(Pageable pageable) {

        Map<String, List<RestaurantListDto>> mainPageData = mainService.getMainPageData(pageable);

        return ResponseEntity.ok(mainPageData);
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<RestaurantListDto>> getRestaurantsNearby(
            @RequestParam double latitude, @RequestParam double longitude, @RequestParam double radius) {
        List<RestaurantListDto> restaurants = mainService.getRestaurantsNearby(latitude, longitude, radius);
        return ResponseEntity.ok(restaurants);
    }

    @GetMapping("/by-region")
    public ResponseEntity<List<RestaurantListDto>> getRestaurantsByRegion(@RequestParam String region) {
        List<RestaurantListDto> restaurantListDtos = mainService.getRestaurantsByRegion(region);
        return ResponseEntity.ok(restaurantListDtos);
    }

    @GetMapping("/with-event")
    public ResponseEntity<List<RestaurantListDto>> getRestaurantsWithEvent(Pageable pageable) {
        List<RestaurantListDto> restaurants = mainService.getRestaurantsWithEvent(pageable);

        return ResponseEntity.ok(restaurants);
    }

    @GetMapping("/with-reviews")
    public ResponseEntity<List<RestaurantListDto>> getRestaurantsWithReviews(Pageable pageable) {
        List<RestaurantListDto> restaurants = mainService.getRestaurantsWithReviews(pageable);

        return ResponseEntity.ok(restaurants);
    }
}
