package com.zero.bwtableback.restaurant.controller;

import com.zero.bwtableback.restaurant.dto.RestaurantListDto;
import com.zero.bwtableback.restaurant.service.MainService;
import com.zero.bwtableback.restaurant.service.RestaurantSearchService;
import com.zero.bwtableback.security.MemberDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/main/restaurants")
@RequiredArgsConstructor
public class MainController {

    private final MainService mainService;
    private final RestaurantSearchService restaurantSearchService;

    // 아이콘
    // 이달의 맛집
    @GetMapping("/top-of-the-month")
    public ResponseEntity<List<RestaurantListDto>> getTop50RestaurantsByReservationCountThisMonth(Pageable pageable) {
        List<RestaurantListDto> restaurants =
                mainService.getTop50RestaurantsByReservationCountThisMonth(pageable);

        return ResponseEntity.ok(restaurants);
    }

    // 모임 예약
    @GetMapping("/for-group")
    public ResponseEntity<List<RestaurantListDto>> getRestaurantsWithEventSpace(Pageable pageable) {
        List<RestaurantListDto> restaurants = mainService.getRestaurantsWithEventSpace(pageable);

        return ResponseEntity.ok(restaurants);
    }

    // 스페셜 혜택
    @GetMapping("/with-event")
    public ResponseEntity<List<RestaurantListDto>> getRestaurantsWithEvent(Pageable pageable) {
        List<RestaurantListDto> restaurants = mainService.getRestaurantsWithEvent(pageable);

        return ResponseEntity.ok(restaurants);
    }

    // 히든플레이스
    @GetMapping("/hidden-place")
    public ResponseEntity<List<RestaurantListDto>> getRestaurantsByReservationCountDesc(Pageable pageable) {
        List<RestaurantListDto> restaurants = mainService.getRestaurantsByReservationCountDesc(pageable);

        return ResponseEntity.ok(restaurants);
    }

    // 오마카세
    @GetMapping("/omakase")
    public ResponseEntity<List<RestaurantListDto>> getRestaurantsByOmakase(Pageable pageable) {
        List<RestaurantListDto> restaurants = restaurantSearchService.getRestaurantsByCategory("OMAKASE", pageable);

        return ResponseEntity.ok(restaurants);
    }

    // 중식
    @GetMapping("/chinese")
    public ResponseEntity<List<RestaurantListDto>> getRestaurantsByChinese(Pageable pageable) {
        List<RestaurantListDto> restaurants = restaurantSearchService.getRestaurantsByCategory("CHINESE", pageable);

        return ResponseEntity.ok(restaurants);
    }

    // 파인다이닝
    @GetMapping("/fine-dining")
    public ResponseEntity<List<RestaurantListDto>> getRestaurantsByFineDining(Pageable pageable) {
        List<RestaurantListDto> restaurants = restaurantSearchService.getRestaurantsByCategory("FINE_DINING", pageable);

        return ResponseEntity.ok(restaurants);
    }

    // 파스타
    @GetMapping("/pasta")
    public ResponseEntity<List<RestaurantListDto>> getRestaurantsByPasta(Pageable pageable) {
        List<RestaurantListDto> restaurants = restaurantSearchService.getRestaurantsByMenu("파스타", pageable);

        return ResponseEntity.ok(restaurants);
    }

    // 어디로 가시나요?
    // 내주변
    @GetMapping("/nearby")
    public ResponseEntity<List<RestaurantListDto>> getRestaurantsNearby(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam double radius) {
        List<RestaurantListDto> restaurants = mainService.getRestaurantsNearby(latitude, longitude, radius);

        return ResponseEntity.ok(restaurants);
    }

    // 지역별
    @GetMapping("/by-region")
    public ResponseEntity<List<RestaurantListDto>> getRestaurantsByRegion(@RequestParam String region) {
        List<RestaurantListDto> restaurantListDtos = mainService.getRestaurantsByRegion(region);

        return ResponseEntity.ok(restaurantListDtos);
    }

    // [놓치면 안되는 혜택 가득, 방문자 리얼리뷰 pick, 고객님이 좋아할 매장, 새로 오픈했어요!] 리스트
    @GetMapping
    public ResponseEntity<Map<String, List<RestaurantListDto>>> getMainPageData(Pageable pageable,
                                                                                @AuthenticationPrincipal MemberDetails memberDetails) {
        Long memberId = (memberDetails != null) ? memberDetails.getMemberId() : null;

        Map<String, List<RestaurantListDto>> mainPageData =
                mainService.getMainPageData(pageable, memberId);

        return ResponseEntity.ok(mainPageData);
    }
}
