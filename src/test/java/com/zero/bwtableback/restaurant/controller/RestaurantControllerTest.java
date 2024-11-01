package com.zero.bwtableback.restaurant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zero.bwtableback.restaurant.dto.MenuDto;
import com.zero.bwtableback.restaurant.dto.OperatingHoursDto;
import com.zero.bwtableback.restaurant.dto.RestaurantReqDto;
import com.zero.bwtableback.restaurant.entity.Restaurant;
import com.zero.bwtableback.restaurant.service.RestaurantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@AutoConfigureMockMvc
public class RestaurantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private RestaurantService restaurantService;

    @InjectMocks
    private RestaurantController restaurantController;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
    }

    @Test
    public void registerRestaurant() throws Exception {
        // 테스트 데이터 생성
        RestaurantReqDto reqDto = new RestaurantReqDto();
        reqDto.setName("맛있는 식당");
        reqDto.setAddress("서울시 강남구");
        reqDto.setContact("010-1234-5678");
        reqDto.setOperatingHours(createOperatingHours());
        reqDto.setMenus(createMenus());

        Restaurant savedRestaurant = new Restaurant();
        savedRestaurant.setId(1L);
        savedRestaurant.setName("맛있는 식당");

        when(restaurantService.registerRestaurant(any())).thenReturn(savedRestaurant);

        // 요청 실행
        mockMvc.perform(post("/api/restaurants/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("맛있는 식당"))
                .andExpect(jsonPath("$.message").value("Restaurant registered successfully"));
    }

    private List<OperatingHoursDto> createOperatingHours() {
        List<OperatingHoursDto> operatingHoursList = new ArrayList<>();
        OperatingHoursDto hours = new OperatingHoursDto();
        hours.setDayOfWeek("월요일");
        hours.setOpeningTime(LocalTime.of(10, 0));
        hours.setClosingTime(LocalTime.of(22, 0));
        operatingHoursList.add(hours);
        return operatingHoursList;
    }

    private List<MenuDto> createMenus() {
        List<MenuDto> menuList = new ArrayList<>();
        MenuDto menu = new MenuDto();
        menu.setName("김밥");
        menu.setPrice(5000);
        menu.setDescription("맛있는 김밥입니다.");
        menu.setImageUrl("http://example.com/image.jpg");
        menuList.add(menu);
        return menuList;
    }
}
