package com.zero.bwtableback.restaurant.service;

import com.zero.bwtableback.common.exception.CustomException;
import com.zero.bwtableback.common.exception.ErrorCode;
import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.member.repository.MemberRepository;
import com.zero.bwtableback.reservation.entity.Reservation;
import com.zero.bwtableback.reservation.repository.ReservationRepository;
import com.zero.bwtableback.restaurant.dto.RestaurantListDto;
import com.zero.bwtableback.restaurant.entity.*;
import com.zero.bwtableback.restaurant.repository.AnnouncementRepository;
import com.zero.bwtableback.restaurant.repository.CategoryRepository;
import com.zero.bwtableback.restaurant.repository.FacilityRepository;
import com.zero.bwtableback.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MainService {
    private final AnnouncementRepository announcementRepository;
    private final RestaurantRepository restaurantRepository;
    private final CategoryRepository categoryRepository;
    private final ReservationRepository reservationRepository;
    private final RestaurantSearchService restaurantSearchService;
    private final MemberRepository memberRepository;


    // 아이콘
    // 이달의 맛집
    // 이번달 예약 많은 순 top50
    public List<RestaurantListDto> getTop50RestaurantsByReservationCountThisMonth(Pageable pageable) {
        YearMonth currentMonth = YearMonth.from(LocalDate.now());
        LocalDate firstDayOfMonth = currentMonth.atDay(1);
        LocalDate lastDayOfMonth = currentMonth.atEndOfMonth();

        // 이번달 예약 많은 순으로 식당 조회
        List<Restaurant> restaurants =
                restaurantRepository.findRestaurantsByReservationCountBetweenDates(firstDayOfMonth, lastDayOfMonth, pageable);

        return restaurants.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }


    // 모임 예약
    // 편의시설에 '대관가능'(EVENT_SPACE) 있는 식당
    public List<RestaurantListDto> getRestaurantsWithEventSpace(Pageable pageable) {
        List<Restaurant> restaurants = restaurantSearchService.getRestaurantsByFacility(FacilityType.EVENT_SPACE, pageable);

        return restaurants.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 스페셜 혜택
    // 이벤트 있는 식당
    // "놓치면 안되는 혜택 가득!" 탭이랑 동일

    // 히든플레이스
    // 예약 수 적은 식당
    public List<RestaurantListDto> getRestaurantsByReservationCountDesc(Pageable pageable) {
        List<Restaurant> restaurants = restaurantRepository.findRestaurantsByReservationCount(pageable);

        return restaurants.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
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

    // 놓치면 안되는 혜택 가득! (eventRestaurants)
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

    // 방문자 리얼리뷰 Pick (reviewRestaurants)
    // 리뷰있는 식당
    public List<RestaurantListDto> getRestaurantsWithReviews(Pageable pageable) {
        List<Restaurant> restaurants = restaurantRepository.findRestaurantsWithReviews();

        return restaurants.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 고객님이 좋아할 매장 (recommendations)
    // 로그인한 사용자 : 가장 최근 예약한 식당 카테고리로 조회
    // 로그인하지 않은 사용자 : 인기 카테고리(searchCount 기준)로 조회
    public List<RestaurantListDto> getRecommendedRestaurants(Pageable pageable, Member member) {
        if (member != null) { // 로그인한 사용자
            return getRecommendedRestaurantsForLoggedInUser(member, pageable);
        } else { // 로그인 하지 않은 사용자
            return getPopularRestaurants(pageable);
        }
    }

    // 로그인한 사용자 : 가장 최근 예약한 식당 카테고리로 조회
    public List<RestaurantListDto> getRecommendedRestaurantsForLoggedInUser(Member member, Pageable pageable) {
        Reservation latestReservation = reservationRepository.findTopByMemberOrderByReservationDateDesc(member);

        if (latestReservation == null) {
            return getPopularRestaurants(pageable);
        }

        CategoryType category = latestReservation.getRestaurant().getCategory().getCategoryType();

        Page<Restaurant> restaurants = restaurantRepository.findByCategory_CategoryType(category, pageable);

        return restaurants.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 로그인하지 않은 사용자 : 인기 카테고리(searchCount 기준)로 조회
    public List<RestaurantListDto> getPopularRestaurants(Pageable pageable) {
        Category popularCategory = categoryRepository.findMostPopularCategory()
                .stream()
                .findFirst()
                .orElse(null);

        List<Restaurant> restaurants = restaurantRepository.findByCategory(popularCategory, pageable);

        return restaurants.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 새로 오픈했어요!
    // 식당 등록 날짜 기준 최신순 (newRestaurants)
    public List<RestaurantListDto> getNewRestaurants(Pageable pageable) {
        List<Restaurant> restaurants = restaurantRepository.findAllByOrderByCreatedAtDesc(pageable);

        return restaurants.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private RestaurantListDto convertToDto(Restaurant restaurant) {
        String firstImageUrl = restaurant.getImages().stream()
                .findFirst()
                .map(RestaurantImage::getImageUrl)
                .orElse(null);

        return new RestaurantListDto(
                restaurant.getId(),
                restaurant.getName(),
                restaurant.getAddress(),
                restaurant.getCategory().getCategoryType().name(),
                restaurant.getAverageRating(),
                firstImageUrl);
    }

    // 모든 정보를 한 데이터로 합치기
    public Map<String, List<RestaurantListDto>> getMainPageData(Pageable pageable, Long memberId) {
        Map<String, List<RestaurantListDto>> result = new HashMap<>();

        Member member = null;
        if (memberId != null) {
            member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        }

        result.put("eventRestaurants", getRestaurantsWithEvent(pageable));
        result.put("reviewRestaurants", getRestaurantsWithReviews(pageable));
        result.put("recommendations", getRecommendedRestaurants(pageable, member));
        result.put("newRestaurants", getNewRestaurants(pageable));

        return result;
    }
}
