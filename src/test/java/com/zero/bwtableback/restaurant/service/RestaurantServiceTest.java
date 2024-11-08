//package com.zero.bwtableback.restaurant.service;
//
//import com.zero.bwtableback.restaurant.dto.RestaurantListDto;
//import com.zero.bwtableback.restaurant.entity.Category;
//import com.zero.bwtableback.restaurant.entity.CategoryType;
//import com.zero.bwtableback.restaurant.entity.Restaurant;
//import com.zero.bwtableback.restaurant.repository.HashtagRepository;
//import com.zero.bwtableback.restaurant.repository.RestaurantRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.mockito.MockitoAnnotations;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.Pageable;
//
//import java.util.*;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.mockito.Mockito.*;
//
//
//public class RestaurantServiceTest {
//
//    @Mock
//    private RestaurantRepository restaurantRepository;
//
//    @Mock
//    private HashtagRepository hashtagRepository;
//
//    @InjectMocks
//    private RestaurantService restaurantService;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    void testGetByName() {
//        // given
//        String name = "피자";
//        Pageable pageable = mock(Pageable.class);
//
//        Category category = Category.builder()
//                .categoryType(CategoryType.FAST_FOOD)
//                .searchCount(0)
//                .restaurants(new ArrayList<>())
//                .build();
//
//        Restaurant restaurant = new Restaurant(
//                1L,
//                "피자헛",
//                "피자맛집",
//                "서울시 용산구 123",
//                37.7749,
//                -122.4194,
//                "123-456-7890",
//                null,
//                new ArrayList<>(),
//                "No notice",
//                "pizzahut.com",
//                new HashSet<>(),
//                category,
//                new ArrayList<>(),
//                new ArrayList<>(),
//                new ArrayList<>(),
//                4.5
//        );
//
//        Page<Restaurant> restaurants = new PageImpl<>(Arrays.asList(restaurant));
//
//        when(restaurantRepository.findByNameContainingIgnoreCase(name, pageable))
//                .thenReturn(restaurants);
//
//        // when
//        List<RestaurantListDto> result = restaurantService.getRestaurantsByName(name, pageable);
//
//        // then
//        assertEquals(1, result.size());
//        assertEquals("피자헛", result.get(0).getName());
//    }
//
//    @Test
//    void testGetByCategory() {
//        // given
//        Pageable pageable = mock(Pageable.class);
//
//        Category category = Category.builder()
//                .categoryType(CategoryType.FAST_FOOD)
//                .searchCount(0)
//                .restaurants(new ArrayList<>())
//                .build();
//
//        Restaurant restaurant = new Restaurant(
//                1L,
//                "피자헛",
//                "피자맛집",
//                "서울시 용산구 123",
//                37.7749,
//                -122.4194,
//                "123-456-7890",
//                null,
//                new ArrayList<>(),
//                "No notice",
//                "pizzahut.com",
//                new HashSet<>(),
//                category,
//                new ArrayList<>(),
//                new ArrayList<>(),
//                new ArrayList<>(),
//                4.5
//        );
//
//        Page<Restaurant> restaurants = new PageImpl<>(Arrays.asList(restaurant));
//
//        when(restaurantRepository.findByCategory_CategoryType(CategoryType.FAST_FOOD, pageable))
//                .thenReturn(new PageImpl<>(Collections.singletonList(restaurant)));
//
//        // when
//        List<RestaurantListDto> result = restaurantService.getRestaurantsByCategory("FAST_FOOD", pageable);
//
//        // then
//        assertNotNull(result);
//        assertEquals(1, result.size());
//        assertEquals("피자헛", result.get(0).getName());
//        assertEquals("서울시 용산구 123", result.get(0).getAddress());
//        assertEquals(CategoryType.FAST_FOOD.name(), result.get(0).getCategory());
//    }
//
//    // 해시태그로 검색, 메뉴로 검색 테스트 코드 추가
//}
