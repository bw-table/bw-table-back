package com.zero.bwtableback.restaurant.service;

import com.zero.bwtableback.restaurant.dto.MenuDto;
import com.zero.bwtableback.restaurant.dto.OperatingHoursDto;
import com.zero.bwtableback.restaurant.dto.RestaurantReqDto;
import com.zero.bwtableback.restaurant.entity.*;
import com.zero.bwtableback.restaurant.repository.CategoryRepository;
import com.zero.bwtableback.restaurant.repository.FacilityRepository;
import com.zero.bwtableback.restaurant.repository.HashtagRepository;
import com.zero.bwtableback.restaurant.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class RestaurantServiceImplTest {

    @InjectMocks
    private RestaurantServiceImpl restaurantService;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private FacilityRepository facilityRepository;

    @Mock
    private HashtagRepository hashtagRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testRegisterSuccess() { // 레스토랑 등록 성공 테스트
        // Given
        RestaurantReqDto reqDto = RestaurantReqDto.builder()
                .name("맛있는 음식점")
                .description("맛있는 음식점은 한식당입니다!")
                .address("서울시 용산구")
                .contact("010-1234-1234")
                .closedDay("월요일")
                .categoryId(1L)
                .operatingHours(List.of(new OperatingHoursDto("화요일", LocalTime.of(10, 0), LocalTime.of(22, 0))))
                .images(List.of("image1Url", "image2Url"))
                .menus(List.of(new MenuDto("김치찌개", 8000, "돼지고기 김치찌개 입니다", "imageUrl")))
                .facilities(List.of(1L, 2L))
                .hashtags(List.of(1L, 2L))
                .build();

        // 카테고리 mock behavior 설정
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(new Category(1L, CategoryType.KOREAN, 0, new ArrayList<>())));

        // 시설 mock 설정
        when(facilityRepository.findById(1L)).thenReturn(Optional.of(new Facility(1L, FacilityType.PARKING)));
        when(facilityRepository.findById(2L)).thenReturn(Optional.of(new Facility(2L, FacilityType.WIFI)));

        // 해시태그 mock 설정
        when(hashtagRepository.findById(1L)).thenReturn(Optional.of(new Hashtag(1L, "용산맛집")));
        when(hashtagRepository.findById(2L)).thenReturn(Optional.of(new Hashtag(2L, "든든한한식")));

        // 레스토랑 저장 mock 설정
        when(restaurantRepository.save(any(Restaurant.class))).thenAnswer(invocation -> {
            Restaurant restaurant = invocation.getArgument(0); // save 메서드 인자 가져오기
            restaurant.setId(1L); // mocking: 해당 객체의 아이디를 1L로 설정
            return restaurant; // mock된 객체 반환
        });

        // when
        Restaurant savedRestaurant = restaurantService.registerRestaurant(reqDto);

        // then
        assertNotNull(savedRestaurant);
        assertEquals("맛있는 음식점", savedRestaurant.getName());
    }


    // 유효하지 않은 연락처 형식, 카테고리 Id가 null인 경우, 이미지 url 형식 유효성, 운영시간 유효성 검사, 메뉴 항목 유효성 검사 등
}
