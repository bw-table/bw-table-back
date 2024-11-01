package com.zero.bwtableback.restaurant.controller;

import com.zero.bwtableback.restaurant.dto.RestaurantListDto;
import com.zero.bwtableback.restaurant.dto.RegisterReqDto;
import com.zero.bwtableback.restaurant.dto.RegisterResDto;
import com.zero.bwtableback.restaurant.entity.Restaurant;
import com.zero.bwtableback.restaurant.exception.RestaurantException;
import com.zero.bwtableback.restaurant.service.RestaurantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@Slf4j
public class RestaurantController {

    private final RestaurantService restaurantService;

    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    /**
     * 식당 등록
     *
     * @param reqDto 식당 등록 요청 데이터
     * @return 등록된 식당 정보
     */
    @PostMapping("/new")
    public ResponseEntity<RegisterResDto> registerRestaurant(@RequestBody RegisterReqDto reqDto) {
        System.out.println("식당 등록 컨트롤러 코드");
        try {
            System.out.println("try문 안에서 호출");
            Restaurant savedRestaurant = restaurantService.registerRestaurant(reqDto);

            RegisterResDto resDto = new RegisterResDto(
                    savedRestaurant.getId(),
                    savedRestaurant.getName(),
                    "Restaurant registered successfully"
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(resDto);
        } catch (RestaurantException e) {
            log.error("Error registering restaurant", e);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new RegisterResDto(null, null, e.getMessage()));
        }
    }

    /**
     * 모든 식당 조회
     */
    @GetMapping
    public ResponseEntity<List<RestaurantListDto>> getRestaurants() {
        List<RestaurantListDto> restaurantList = restaurantService.getRestaurants();
        return ResponseEntity.ok(restaurantList);
    }


}
