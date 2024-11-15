package com.zero.bwtableback.restaurant.service;

import com.zero.bwtableback.restaurant.dto.RestaurantListDto;
import com.zero.bwtableback.restaurant.entity.Announcement;
import com.zero.bwtableback.restaurant.entity.CategoryType;
import com.zero.bwtableback.restaurant.entity.Restaurant;
import com.zero.bwtableback.restaurant.repository.AnnouncementRepository;
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

    // 아이콘
    public List<RestaurantListDto> getRestaurantsByOmakase(Pageable pageable) {
        return restaurantService.getRestaurantsByCategory(CategoryType.OMAKASE.name(), pageable);
    }

    public List<RestaurantListDto> getRestaurantsByChinese(Pageable pageable) {
        return restaurantService.getRestaurantsByCategory(CategoryType.CHINESE.name(), pageable);
    }

    public List<RestaurantListDto> getRestaurantsByFineDining(Pageable pageable) {
        return restaurantService.getRestaurantsByCategory(CategoryType.FINE_DINING.name(), pageable);
    }

    public List<RestaurantListDto> getRestaurantsByPasta(Pageable pageable) {
        return restaurantService.getRestaurantsByMenu("파스타", pageable);
    }

    // TODO 어디로 가시나요?
    // 내주변 : 프론트에서 위도/경도값 받아와서 구현
    // 지역 : 주소 필터링(정규표현식)해서 지역별 식당 조회 구현

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

    // TODO 새로 오픈했어요!
    // 식당 등록 날짜 기준

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

        // 아이콘
        result.put("오마카세", getRestaurantsByOmakase(pageable));
        result.put("중식", getRestaurantsByChinese(pageable));
        result.put("파인다이닝", getRestaurantsByFineDining(pageable));
        result.put("파스타", getRestaurantsByPasta(pageable));

        result.put("어디로 가시나요?", null);

        result.put("놓치면 안되는 혜택 가득!", getRestaurantsWithEvent(pageable));
        result.put("방문자 리얼리뷰 Pick", getRestaurantsWithReviews(pageable));
        // TODO 나머지 두 메서드 작성 다 되면 추가

        return result;
    }
}
