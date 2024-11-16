package com.zero.bwtableback.restaurant.service;

import com.zero.bwtableback.restaurant.dto.RestaurantListDto;
import com.zero.bwtableback.restaurant.entity.*;
import com.zero.bwtableback.restaurant.repository.AnnouncementRepository;
import com.zero.bwtableback.restaurant.repository.FacilityRepository;
import com.zero.bwtableback.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MainService {

    private final RestaurantService restaurantService;

    private final AnnouncementRepository announcementRepository;
    private final RestaurantRepository restaurantRepository;
    private final FacilityRepository facilityRepository;

    // 아이콘
    // 이달의 맛집
    // TODO 어떤 기준으로 할건지?

    // 모임 예약
    // 편의시설에 '대관가능'(EVENT_SPACE) 있는 식당
    public List<RestaurantListDto> getRestaurantsWithEventSpace(Pageable pageable) {
        List<Restaurant> restaurants = restaurantService.getRestaurantsByFacility(FacilityType.EVENT_SPACE, pageable);

        return restaurants.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 스페셜 혜택
    // 이벤트 있는 식당
    // TODO "놓치면 안되는 혜택 가득!" 탭이랑 동일?

    // 히든플레이스
    // 예약 수 적은 식당
    public List<RestaurantListDto> getRestaurantsByReservationCountDesc(Pageable pageable) {
        List<Restaurant> restaurants = restaurantRepository.findRestaurantsByReservationCount(pageable);

        return restaurants.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 오마카세
    public List<RestaurantListDto> getRestaurantsByOmakase(Pageable pageable) {
        return restaurantService.getRestaurantsByCategory(CategoryType.OMAKASE.name(), pageable);
    }

    // 중식
    public List<RestaurantListDto> getRestaurantsByChinese(Pageable pageable) {
        return restaurantService.getRestaurantsByCategory(CategoryType.CHINESE.name(), pageable);
    }

    // 파인다이닝
    public List<RestaurantListDto> getRestaurantsByFineDining(Pageable pageable) {
        return restaurantService.getRestaurantsByCategory(CategoryType.FINE_DINING.name(), pageable);
    }

    // 파스타
    public List<RestaurantListDto> getRestaurantsByPasta(Pageable pageable) {
        return restaurantService.getRestaurantsByMenu("파스타", pageable);
    }

    // 어디로 가시나요?
    // 1. 내주변 : 프론트에서 위도/경도값 받아와서 구현 (Haversine 공식 사용)
    public List<RestaurantListDto> getRestaurantsNearby(double latitude, double longitude, double radius) {
        List<Restaurant> restaurants = restaurantRepository.findRestaurantsNearby(latitude, longitude, radius);

        return restaurants.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 2. 지역 : 주소에 지역명 포함된 경우 조회
    // (주소 필터링(정규표현식)해서 지역별 식당 조회 구현)
    public List<RestaurantListDto> getRestaurantsByRegion(String region) {
        List<Restaurant> restaurants = restaurantRepository.findByAddressContaining(region);

        return restaurants.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 놓치면 안되는 혜택 가득!
    // 이벤트 진행중인 식당
    // Announcement 엔티티의 event 필드가 true인 식당
    public List<RestaurantListDto> getRestaurantsWithEvent(Pageable pageable) {
        List<Announcement> eventAnnouncements = announcementRepository.findByEventTrue();

        Set<Long> restaurantIds = eventAnnouncements.stream()
                .map(Announcement::getRestaurant)
                .map(Restaurant::getId)
                .collect(Collectors.toSet());

        Page<Restaurant> restaurants = restaurantRepository.findByIdIn(restaurantIds, pageable);

        return restaurants.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 방문자 리얼리뷰 Pick
    // 리뷰있는 식당
    public List<RestaurantListDto> getRestaurantsWithReviews(Pageable pageable) {
        List<Restaurant> restaurants = restaurantRepository.findRestaurantsWithReviews();

        return restaurants.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // TODO 고객님이 좋아할 매장
    // 로그인한 사용자 : 가장 최근 예약한 식당 카테고리로 조회
    // 로그인하지 않은 사용자 : ?


    // 식당 등록 날짜 기준 최신순
    public List<RestaurantListDto> getNewRestaurants(Pageable pageable) {
        List<Restaurant> restaurants = restaurantRepository.findAllByOrderByCreatedAtDesc(pageable);

        return restaurants.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private RestaurantListDto convertToDto(Restaurant restaurant) {
        return new RestaurantListDto(
                restaurant.getId(),
                restaurant.getName(),
                restaurant.getAddress(),
                restaurant.getCategory().getCategoryType().name(),
                restaurant.getAverageRating());
    }

    // 모든 정보를 한 데이터로 합치기
    public Map<String, List<RestaurantListDto>> getMainPageData(Pageable pageable) {
        Map<String, List<RestaurantListDto>> result = new HashMap<>();

        // TODO 아이콘 리스트들은 메인페이지 데이터에 바로 보이지 않기 때문에 필요없지 않나?
        // 아이콘
//        result.put("이달의 맛집", null);
//        result.put("모임 예약", getRestaurantsWithEventSpace(pageable));
//        result.put("스페셜 혜택", getRestaurantsWithEvent(pageable));
//        result.put("히든플레이스", getRestaurantsByReservationCountDesc(pageable));
//        result.put("오마카세", getRestaurantsByOmakase(pageable));
//        result.put("중식", getRestaurantsByChinese(pageable));
//        result.put("파인다이닝", getRestaurantsByFineDining(pageable));
//        result.put("파스타", getRestaurantsByPasta(pageable));
//
//        // 어디로 가시나요?
//        Map<String, List<RestaurantListDto>> whereToGo = new HashMap<>();
//
//        // 내주변
//        if (latitude != null && longitude != null) {
//            whereToGo.put("내주변", getRestaurantsNearby(latitude, longitude, 10.0)); // 10km 반경
//        }
//
//        // 지역별
//        if (region != null && !region.isEmpty()) {
//            whereToGo.put("지역별", getRestaurantsByRegion(region));
//        }
//
//        result.put("어디로 가시나요?", whereToGo);

        result.put("놓치면 안되는 혜택 가득!", getRestaurantsWithEvent(pageable));
        result.put("방문자 리얼리뷰 Pick", getRestaurantsWithReviews(pageable));
        result.put("고객님이 좋아할 매장", null);
        result.put("새로 오픈했어요!", getNewRestaurants(pageable));

        return result;
    }
}
