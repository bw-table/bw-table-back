package com.zero.bwtableback.restaurant.controller;

import com.zero.bwtableback.chat.dto.ChatRoomCreateResDto;
import com.zero.bwtableback.chat.service.ChatService;
import com.zero.bwtableback.restaurant.dto.*;
import com.zero.bwtableback.restaurant.entity.Restaurant;
import com.zero.bwtableback.restaurant.exception.RestaurantException;
import com.zero.bwtableback.restaurant.service.AnnouncementService;
import com.zero.bwtableback.restaurant.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
@Slf4j
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final AnnouncementService announcementService;

    // 식당 등록
    // TODO: 식당 등록 후 응답형식 결정 필요
//    @PostMapping("/new")
//    public ResponseEntity<?> registerRestaurant(@RequestBody @Valid RegisterReqDto reqDto) {
//
//        try {
//            Restaurant savedRestaurant = restaurantService.registerRestaurant(reqDto);
//
//            RegisterResDto resDto = new RegisterResDto(
//                    savedRestaurant.getId(),
//                    savedRestaurant.getName(),
//                    "Restaurant registered successfully"
//            );
//            return ResponseEntity.status(HttpStatus.CREATED).body(resDto);
//        } catch (RestaurantException e) {
//            log.error("Error registering restaurant", e);
//
//            Map<String, String> errorResponse = new HashMap<>();
//            errorResponse.put("message", e.getMessage());
//
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
//        }
//    }

    @PostMapping("/new")
    public ResponseEntity<?> registerRestaurant(@RequestBody @Valid RegisterReqDto reqDto) {
        // TODO: 유효성 검사 오류 발생 시 처리
//    if (bindingResult.hasErrors()) {
//        Map<String, String> errorResponse = new HashMap<>();
//        bindingResult.getFieldErrors().forEach(error ->
//                errorResponse.put(error.getField(), error.getDefaultMessage())
//        );
//
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
//    }

        try {
            Restaurant savedRestaurant = restaurantService.registerRestaurant(reqDto);

            // 직접 Restaurant 객체를 반환
            return ResponseEntity.status(HttpStatus.CREATED).body(savedRestaurant);
        } catch (RestaurantException e) {
            log.error("Error registering restaurant", e);

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    // TODO: 식당 정보 수정


    // 모든 식당 조회
    @GetMapping
    public ResponseEntity<List<RestaurantListDto>> getRestaurants(Pageable pageable) {
        List<RestaurantListDto> restaurantList = restaurantService.getRestaurants(pageable);
        return ResponseEntity.ok(restaurantList);
    }

    // 이름으로 식당 검색
    @GetMapping("/search")
    public ResponseEntity<List<RestaurantListDto>> getRestaurantByName(
            @RequestParam String name,
            Pageable pageable) {
        List<RestaurantListDto> restaurants = restaurantService.getRestaurantsByName(name, pageable);
        return ResponseEntity.ok(restaurants);
    }

    // 업종으로 식당 검색
    @GetMapping("/search/categories")
    public ResponseEntity<List<RestaurantListDto>> getRestaurantsByCategory(
            @RequestParam String category,
            Pageable pageable) {
        List<RestaurantListDto> restaurants = restaurantService.getRestaurantsByCategory(category, pageable);
        return ResponseEntity.ok(restaurants);
    }

    // 메뉴로 식당 검색
    @GetMapping("/search/menus")
    public ResponseEntity<List<RestaurantListDto>> getRestaurantsByMenu(
            @RequestParam String menu,
            Pageable pageable) {
        List<RestaurantListDto> restaurants = restaurantService.getRestaurantsByMenu(menu, pageable);
        return ResponseEntity.ok(restaurants);
    }

    // 해시태그로 식당 검색
    @GetMapping("/search/hashtags")
    public ResponseEntity<List<RestaurantListDto>> getRestaurantsByHashtag(
            @RequestParam String hashtag,
            Pageable pageable) {
        List<RestaurantListDto> restaurants = restaurantService.getRestaurantsByHashtag(hashtag, pageable);
        return ResponseEntity.ok(restaurants);
    }

    // 해시태그 자동완성
    @GetMapping("/search/hashtags/suggestions")
    public ResponseEntity<List<String>> getHashtagSuggestions(@RequestParam String hashtag) {
        List<String> suggestions = restaurantService.getHashtagSuggestions(hashtag);
        return ResponseEntity.ok(suggestions);
    }

    // 식당 상세정보 조회
    @GetMapping("/{id}")
    public ResponseEntity<RestaurantInfoDto> getRestaurantById(@PathVariable Long id) {
        RestaurantInfoDto infoDto = restaurantService.getRestaurantById(id);
        return ResponseEntity.ok(infoDto);
    }

    // 공지 생성
    @PostMapping("/{restaurantId}/announcements")
    public ResponseEntity<AnnouncementResDto> createAnnouncement(@PathVariable Long restaurantId,
                                                                 @RequestBody AnnouncementReqDto reqDto) {
        AnnouncementReqDto updatedReqDto = AnnouncementReqDto.builder()
                .restaurantId(restaurantId)
                .title(reqDto.getTitle())
                .content(reqDto.getContent())
                .event(reqDto.isEvent())
                .build();

        AnnouncementResDto resDto = announcementService.createAnnouncement(updatedReqDto);

        return ResponseEntity.ok(resDto);
    }

    // 식당 공지 목록 조회
    @GetMapping("/{restaurantId}/announcements")
    public ResponseEntity<List<AnnouncementDetailDto>> getAnnouncementsByRestaurantId(
            @PathVariable Long restaurantId, Pageable pageable) {
        List<AnnouncementDetailDto> announcements = announcementService.getAnnouncementsByRestaurantId(restaurantId, pageable);

        return ResponseEntity.ok(announcements);
    }

    // 특정 식당의 모든 채팅방 조회
    @GetMapping("/{restaurantId}/chats")
    public ResponseEntity<Page<ChatRoomCreateResDto>> getAllChatRooms(@PathVariable Long restaurantId, Pageable pageable) {
        Page<ChatRoomCreateResDto> chatRooms = restaurantService.getChatRoomsByRestaurantId(restaurantId, pageable);
        return ResponseEntity.ok(chatRooms);
    }
}
