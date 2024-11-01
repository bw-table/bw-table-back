package com.zero.bwtableback.restaurant.controller;

import com.zero.bwtableback.restaurant.dto.RestaurantReqDto;
import com.zero.bwtableback.restaurant.dto.RestaurantResDto;
import com.zero.bwtableback.restaurant.entity.Restaurant;
import com.zero.bwtableback.restaurant.exception.RestaurantException;
import com.zero.bwtableback.restaurant.service.RestaurantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/restaurants")
@Slf4j
public class RestaurantController {

    private final RestaurantService restaurantService;

    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    /**
     * 가게 등록
     *
     * @param reqDto 가게 등록 요청 데이터
     * @return 등록된 가게 정보
     */
    @PostMapping("/new")
    public ResponseEntity<RestaurantResDto> registerRestaurant(@RequestBody RestaurantReqDto reqDto) {
        System.out.println("레스토랑 등록 컨트롤러 코드");
        try {
            System.out.println("try문 안에서 호출");
            Restaurant savedRestaurant = restaurantService.registerRestaurant(reqDto);

            RestaurantResDto resDto = new RestaurantResDto(
                    savedRestaurant.getId(),
                    savedRestaurant.getName(),
                    "Restaurant registered successfully"
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(resDto);
        } catch (RestaurantException e) {
            log.error("Error registering restaurant", e);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new RestaurantResDto(null, null, e.getMessage()));
        }
    }

}
