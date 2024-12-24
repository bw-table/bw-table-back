package com.zero.bwtableback.restaurant.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.zero.bwtableback.chat.dto.ChatRoomCreateResDto;
import com.zero.bwtableback.restaurant.dto.*;
import com.zero.bwtableback.restaurant.exception.RestaurantException;
import com.zero.bwtableback.restaurant.service.RestaurantSearchService;
import com.zero.bwtableback.restaurant.service.RestaurantService;
import com.zero.bwtableback.security.MemberDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
@Slf4j
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final RestaurantSearchService restaurantSearchService;

    // 식당 등록
    @PreAuthorize("hasrole('OWNER')")
    @PostMapping("/new")
    public ResponseEntity<?> registerRestaurant(
//            @ModelAttribute RestaurantRegistrationDto registrationDto,
            @RequestPart("name") String name,
            @RequestPart("description") String description,
            @RequestPart("address") String address,
            @RequestPart("latitude") Double latitude,
            @RequestPart("longitude") Double longitude,
            @RequestPart("contact") String contact,
            @RequestPart("closedDay") String closedDay,
            @RequestPart("category") String category,
            @RequestPart("info") String info,
            @RequestPart("link") String link,
            @RequestPart("deposit") Integer deposit,
            @RequestPart("menus") List<MenuRegisterDto> menus,
            @RequestPart("operatingHours") List<OperatingHoursDto> operatingHours,
            @RequestPart("facilities") List<String> facilities,
            @RequestPart("hashtags") List<String> hashtags,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestPart(value = "menuImages", required = false) List<MultipartFile> menuImages,
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
//        System.out.println("name " + registrationDto.getName());
        System.out.println("name" + name);
//        System.out.println("description" + registrationDto.getDescription());
        System.out.println("description" + description);

//        System.out.println("address" + registrationDto.getAddress());

//        System.out.println("Images" + registrationDto.getImages());
//        System.out.println("Menus" +registrationDto.getMenus());
//        System.out.println("MenuImages" +registrationDto.getMenuImages());

        try {
//            ObjectMapper objectMapper = new ObjectMapper();
//            objectMapper.registerModule(new JavaTimeModule());
//            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
//
//            RestaurantReqDto reqDto = objectMapper.readValue(registrationDto.getRestaurant(), RestaurantReqDto.class);
//            System.out.println(reqDto.getAddress());
//            List<MenuRegisterDto> menus = objectMapper.readValue(registrationDto.getMenus(), new TypeReference<List<MenuRegisterDto>>() {});
//            System.out.println(menus.get(0).getName());
//            MultipartFile[] images = registrationDto.getImages();
//            List<MultipartFile> menuImages = registrationDto.getMenuImages();

//            reqDto.setImages(reqDto.getImages());
//            reqDto.setMenus(menus);
//
//            RestaurantRegisterResDto savedRestaurant =
//                    restaurantService.registerRestaurant(reqDto, images, menus, menuImages, memberDetails.getMemberId());

            return null;
//            return ResponseEntity.ok(savedRestaurant);
        } catch (RestaurantException e) {
            // 레스토랑 등록 실패
            log.error("Error registering restaurant", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
//        }
//        catch (IOException e) {
//            // 파일 업로드 or IO 관련 오류
//            log.error("File upload error", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File upload failed");
        } catch (Exception e) {
            // 예상치 못한 예외
            log.error("버킷 오류: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error occurred");
        }
    }

    // 식당 정보 수정
    @PreAuthorize("hasrole('OWNER')")
    @PutMapping("/{id}")
    public ResponseEntity<RestaurantRegisterResDto> updateRestaurant(
            @PathVariable("id") Long restaurantId,
            @RequestPart("restaurant") RestaurantUpdateReqDto reqDto,
            @RequestPart(value = "images", required = false) MultipartFile[] images,
            @RequestPart(value = "menuImages", required = false) List<MultipartFile> menuImages,
            @AuthenticationPrincipal MemberDetails memberDetails)
            throws IOException {

        RestaurantRegisterResDto updatedRestaurant =
                restaurantService.updateRestaurant(restaurantId, reqDto, images, menuImages, memberDetails.getMemberId());

        return ResponseEntity.ok(updatedRestaurant);
    }

    // 모든 식당 조회
    @GetMapping
    public ResponseEntity<List<RestaurantListDto>> getRestaurants(Pageable pageable) {
        List<RestaurantListDto> restaurantList = restaurantSearchService.getRestaurants(pageable);
        return ResponseEntity.ok(restaurantList);
    }

    // 이름으로 식당 검색
    @GetMapping("/search")
    public ResponseEntity<List<RestaurantListDto>> getRestaurantByName(
            @RequestParam String name,
            Pageable pageable) {
        List<RestaurantListDto> restaurants = restaurantSearchService.getRestaurantsByName(name, pageable);
        return ResponseEntity.ok(restaurants);
    }

    // 업종으로 식당 검색
    @GetMapping("/search/categories")
    public ResponseEntity<List<RestaurantListDto>> getRestaurantsByCategory(
            @RequestParam String category,
            Pageable pageable) {
        List<RestaurantListDto> restaurants = restaurantSearchService.getRestaurantsByCategory(category, pageable);
        return ResponseEntity.ok(restaurants);
    }

    // 메뉴로 식당 검색
    @GetMapping("/search/menus")
    public ResponseEntity<List<RestaurantListDto>> getRestaurantsByMenu(
            @RequestParam String menu,
            Pageable pageable) {
        List<RestaurantListDto> restaurants = restaurantSearchService.getRestaurantsByMenu(menu, pageable);
        return ResponseEntity.ok(restaurants);
    }

    // 해시태그로 식당 검색
    @GetMapping("/search/hashtags")
    public ResponseEntity<List<RestaurantListDto>> getRestaurantsByHashtag(
            @RequestParam String hashtag,
            Pageable pageable) {
        List<RestaurantListDto> restaurants = restaurantSearchService.getRestaurantsByHashtag(hashtag, pageable);
        return ResponseEntity.ok(restaurants);
    }

    // 해시태그 자동완성
    @GetMapping("/search/hashtags/suggestions")
    public ResponseEntity<List<String>> getHashtagSuggestions(@RequestParam String hashtag) {
        List<String> suggestions = restaurantSearchService.getHashtagSuggestions(hashtag);
        return ResponseEntity.ok(suggestions);
    }

    // 식당 상세정보 조회
    @GetMapping("/{id}")
    public ResponseEntity<RestaurantDetailDto> getRestaurantById(@PathVariable Long id) {
        RestaurantDetailDto detailDto = restaurantService.getRestaurantById(id);
        return ResponseEntity.ok(detailDto);
    }

    // 특정 식당의 모든 채팅방 조회
    @GetMapping("/{restaurantId}/chats")
    public ResponseEntity<Page<ChatRoomCreateResDto>> getAllChatRooms(@PathVariable Long restaurantId, Pageable pageable) {
        Page<ChatRoomCreateResDto> chatRooms = restaurantService.getChatRoomsByRestaurantId(restaurantId, pageable);
        return ResponseEntity.ok(chatRooms);
    }
}
