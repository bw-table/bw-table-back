package com.zero.bwtableback.restaurant.controller;

import com.zero.bwtableback.restaurant.dto.RestaurantInfoDto;
import com.zero.bwtableback.restaurant.dto.RestaurantListDto;
import com.zero.bwtableback.restaurant.dto.RegisterReqDto;
import com.zero.bwtableback.restaurant.dto.RegisterResDto;
import com.zero.bwtableback.restaurant.entity.Restaurant;
import com.zero.bwtableback.restaurant.exception.RestaurantException;
import com.zero.bwtableback.restaurant.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/restaurants")
@Slf4j
public class RestaurantController {

    private final RestaurantService restaurantService;

    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    // 식당 등록
    @PostMapping("/new")
    public ResponseEntity<?> registerRestaurant(@RequestBody @Valid RegisterReqDto reqDto) {
        // TODO: 유효성 검사 오류 발생 시 처리
//        if (bindingResult.hasErrors()) {
//            Map<String, String> errorResponse = new HashMap<>();
//            bindingResult.getFieldErrors().forEach(error ->
//                    errorResponse.put(error.getField(), error.getDefaultMessage())
//            );
//
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
//        }

        try {
            Restaurant savedRestaurant = restaurantService.registerRestaurant(reqDto);

            RegisterResDto resDto = new RegisterResDto(
                    savedRestaurant.getId(),
                    savedRestaurant.getName(),
                    "Restaurant registered successfully"
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(resDto);
        } catch (RestaurantException e) {
            log.error("Error registering restaurant", e);

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    // 모든 식당 조회
    @GetMapping
    public ResponseEntity<List<RestaurantListDto>> getRestaurants() {
        List<RestaurantListDto> restaurantList = restaurantService.getRestaurants();
        return ResponseEntity.ok(restaurantList);
    }

    // 식당 상세정보 조회
    @GetMapping("/{id}")
    public ResponseEntity<RestaurantInfoDto> getRestaurantById(@PathVariable Long id) {
        RestaurantInfoDto infoDto = restaurantService.getRestaurantById(id);
        return ResponseEntity.ok(infoDto);
    }
}
