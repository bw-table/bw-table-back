package com.zero.bwtableback.restaurant.service;

import com.zero.bwtableback.chat.dto.ChatRoomCreateResDto;
import com.zero.bwtableback.chat.repository.ChatRoomRepository;
import com.zero.bwtableback.common.service.ImageUploadService;
import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.member.entity.Role;
import com.zero.bwtableback.member.repository.MemberRepository;
import com.zero.bwtableback.restaurant.dto.*;
import com.zero.bwtableback.restaurant.entity.*;
import com.zero.bwtableback.restaurant.exception.RestaurantException;
import com.zero.bwtableback.restaurant.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.swing.text.html.Option;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final MenuRepository menuRepository;
    private final CategoryRepository categoryRepository;
    private final FacilityRepository facilityRepository;
    private final HashtagRepository hashtagRepository;
    private final RestaurantImageRepository restaurantImageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ImageUploadService imageUploadService;
    private final MemberRepository memberRepository;

    // 등록
    // TODO 테스트 끝나면 응답형식 RegisterResDto로 수정 예정
//    public Restaurant registerRestaurant(RegisterReqDto reqDto) {
//        reqDto.validate();
//
//        // 카테고리 설정
//        Category category = null;
//        if (reqDto.getCategory() != null) {
//            try {
//                CategoryType categoryType = CategoryType.valueOf(reqDto.getCategory().toUpperCase());
//                category = categoryRepository.findByCategoryType(categoryType)
//                        .orElseThrow(() -> new EntityNotFoundException("Category not found"));
//            } catch (IllegalArgumentException e) {
//                throw new RestaurantException("Invalid category type provided: " + reqDto.getCategory());
//            }
//        }
//
//        // 주소, 연락처 중복 체크
//        if (restaurantRepository.existsByAddress(reqDto.getAddress())) {
//            throw new RestaurantException("Restaurant with this address already exists.");
//        }
//        if (restaurantRepository.existsByContact(reqDto.getContact())) {
//            throw new RestaurantException("Restaurant with this contact number already exists.");
//        }
//
//        // 레스토랑 객체 생성
//        Restaurant restaurant = Restaurant.builder()
//                .name(reqDto.getName())
//                .description(reqDto.getDescription())
//                .address(reqDto.getAddress())
//                .contact(reqDto.getContact())
//                .closedDay(reqDto.getClosedDay())
//                .link(reqDto.getLink())
//                .info(reqDto.getInfo())
//                .deposit(reqDto.getDeposit())
//                .category(category)
//                .images(new HashSet<>())
//                .operatingHours(new ArrayList<>())
//                .menus(new ArrayList<>())
//                .facilities(new ArrayList<>())
//                .hashtags(new ArrayList<>())
//                .build();
//
//        // 영업시간 설정
//        List<OperatingHours> operatingHours = reqDto.getOperatingHours().stream()
//                .map(hoursDto -> OperatingHours.builder()
//                        .dayOfWeek(hoursDto.getDayOfWeek())
//                        .openingTime(hoursDto.getOpeningTime())
//                        .closingTime(hoursDto.getClosingTime())
//                        .restaurant(restaurant)
//                        .build())
//                .collect(Collectors.toList());
//        restaurant.setOperatingHours(operatingHours);
//
//        // 메뉴 설정
//        List<Menu> menus = reqDto.getMenus().stream()
//                .map(menuDto -> Menu.builder()
//                        .name(menuDto.getName())
//                        .price(menuDto.getPrice())
//                        .description(menuDto.getDescription())
//                        .imageUrl(menuDto.getImageUrl())
//                        .restaurant(restaurant)
//                        .build())
//                .collect(Collectors.toList());
//        restaurant.setMenus(menus);
//
//        // 편의시설 설정
//        List<Facility> facilities =  reqDto.getFacilities().stream()
//                .map(facilityType -> {
//                    FacilityType type = FacilityType.valueOf(facilityType);
//                    return facilityRepository.findByFacilityType(type)
//                            .orElseThrow(() -> new EntityNotFoundException("Facility not found"));
//                })
//                .collect(Collectors.toList());
//        restaurant.setFacilities(facilities);
//
//        // 해시태그 설정
//        List<Hashtag> hashtags = reqDto.getHashtags().stream()
//                .map(tag -> hashtagRepository.findByName(tag)
//                        .orElseGet(() -> {
//                            Hashtag newHashtag = new Hashtag(tag);
//                            return hashtagRepository.save(newHashtag);
//                        }))
//                .collect(Collectors.toList());
//        restaurant.setHashtags(hashtags);
//
//        // 레스토랑 저장
//        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
//
//        // 이미지 설정
//        Set<RestaurantImage> images = reqDto.getImages().stream()
//                .map(imageUrl -> RestaurantImage.builder()
//                        .imageUrl(imageUrl)
//                        .restaurant(savedRestaurant)
//                        .build())
//                .collect(Collectors.toSet());
//        savedRestaurant.setImages(images);
//
//        if (images.isEmpty()) {
//            System.out.println("No images to save.");
//        } else {
//            restaurantImageRepository.saveAll(images);
//        }
//
//        return savedRestaurant;
//    }

    public Restaurant registerRestaurant(RegisterReqDto reqDto,
                                         MultipartFile[] images,
                                         List<MenuRegisterDto> menus,
                                         List<MultipartFile> menuImages) throws IOException {

        reqDto.validate();  // TODO 간단한 테스트 후 삭제 예정

//        Member member = memberRepository.findById(memberId)
//                .orElseThrow(() -> new EntityNotFoundException("Member not found"));
//
//        if (member.getRole() != Role.OWNER) {
//            throw new AccessDeniedException("Only owners can register restaurants");
//        }

        if (restaurantRepository.existsByAddress(reqDto.getAddress())) {
            throw new RestaurantException("Restaurant with this address already exists.");
        }
        if (restaurantRepository.existsByContact(reqDto.getContact())) {
            throw new RestaurantException("Restaurant with this contact number already exists.");
        }

        Category category = assignCategory(reqDto.getCategory());

        // 레스토랑 객체 생성
        Restaurant restaurant = Restaurant.builder()
                .name(reqDto.getName())
                .description(reqDto.getDescription())
                .address(reqDto.getAddress())
                .latitude(reqDto.getLatitude())
                .longitude(reqDto.getLongitude())
                .contact(reqDto.getContact())
//                .closedDay(reqDto.getClosedDay())
                .link(reqDto.getLink())
                .info(reqDto.getInfo())
                .deposit(reqDto.getDeposit())
                .impCode(reqDto.getImpCode())
                .category(category)
                .images(new HashSet<>())
                .operatingHours(new ArrayList<>())
                .menus(new ArrayList<>())
                .facilities(new ArrayList<>())
                .hashtags(new ArrayList<>())
                .build();

        List<OperatingHours> operatingHours = assignOperatingHours(reqDto.getOperatingHours(), restaurant);
        restaurant.setOperatingHours(operatingHours);

        // 휴무일
        String closedDays = getClosedDays(operatingHours);
        restaurant.setClosedDay(closedDays);

        List<Menu> menuList = assignMenu(menus, restaurant);
        restaurant.setMenus(menuList);

        List<Facility> facilities = assignFacilities(reqDto.getFacilities());
        restaurant.setFacilities(facilities);

        List<Hashtag> hashtags = assignHashtags(reqDto.getHashtags());
        restaurant.setHashtags(hashtags);

        Restaurant savedRestaurant = restaurantRepository.save(restaurant);

        // 식당 이미지 설정
        Set<RestaurantImage> restaurantImages = new HashSet<>();
        if (images != null && images.length > 0) {

            List<String> imageUrls = imageUploadService.uploadRestaurantImages(savedRestaurant.getId(), images);

            for (String imageUrl: imageUrls) {
                RestaurantImage restaurantImage = new RestaurantImage(imageUrl, savedRestaurant);
                restaurantImages.add(restaurantImage);
            }
            restaurantImageRepository.saveAll(restaurantImages);
        }

        // 메뉴 이미지 설정
        for (int i = 0; i < menus.size(); i++) {
            Menu menu = savedRestaurant.getMenus().get(i);
            if (menuImages != null && menuImages.size() > i) {
                String menuImageUrl = imageUploadService.uploadMenuImage(savedRestaurant.getId(), menu.getId(), menuImages.get(i));
                menu.setImageUrl(menuImageUrl);  // 메뉴 이미지 URL 설정
                menuRepository.save(menu);  // 메뉴 저장
            }
        }

        return savedRestaurant;
    }

    // 식당 정보 수정
    // FIXME: 현재 확인용으로 Restaurant 객체 반환하도록 작성 -> 추후 응답객체 변경
    public Restaurant updateRestaurant(Long id,
                                       UpdateReqDto reqDto,
                                       MultipartFile[] newImages,
                                       Map<Long, MultipartFile> newMenuImages) throws IOException {

        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found"));

        if (reqDto.getName() != null) {
            restaurant.setName(reqDto.getName());
        }

        if (reqDto.getDescription() != null) {
            restaurant.setDescription(reqDto.getDescription());
        }

        if (reqDto.getAddress() != null && !restaurant.getAddress().equals(reqDto.getAddress())) {
            if (restaurantRepository.existsByAddress(reqDto.getAddress())) {
                throw new RestaurantException("Restaurant with this address already exists");
            }
            restaurant.setAddress(reqDto.getAddress());
        }

        if (reqDto.getLatitude() != null) {
            restaurant.setLatitude(reqDto.getLatitude());
        }

        if (reqDto.getLongitude() != null) {
            restaurant.setLongitude(reqDto.getLongitude());
        }

        if (reqDto.getContact() != null && !restaurant.getContact().equals(reqDto.getContact())) {
            if (restaurantRepository.existsByContact(reqDto.getContact())) {
                throw new RestaurantException("Restaurant with this contact already exists");
            }
            restaurant.setContact(reqDto.getContact());
        }

        if (reqDto.getClosedDay() != null) {
            restaurant.setClosedDay(reqDto.getClosedDay());
        }

        if (reqDto.getLink() != null) {
            restaurant.setLink(reqDto.getLink());
        }

        if (reqDto.getInfo() != null) {
            restaurant.setInfo(reqDto.getInfo());
        }

        if (reqDto.getDeposit() != null) {
            restaurant.setDeposit(reqDto.getDeposit());
        }

        if (reqDto.getImpCode() != null) {
            restaurant.setImpCode(reqDto.getImpCode());
        }

        if (reqDto.getCategory() != null) {
            Category category = assignCategory(reqDto.getCategory());
            restaurant.setCategory(category);
        }

        if (reqDto.getFacilities() != null) {
            List<Facility> facilities = assignFacilities(reqDto.getFacilities());
            restaurant.setFacilities(facilities);
        }

        if (reqDto.getHashtags() != null) {
            List<Hashtag> hashtags = assignHashtags(reqDto.getHashtags());
            restaurant.setHashtags(hashtags);
        }

//        if (reqDto.getMenus() != null) {
//            List<Menu> menus = assignMenu(reqDto.getMenus(), restaurant);
//            restaurant.setMenus(menus);
//        }

        if (reqDto.getOperatingHours() != null) {
            List<OperatingHours> operatingHours = assignOperatingHours(reqDto.getOperatingHours(), restaurant);
            restaurant.setOperatingHours(operatingHours);
        }

        if (newImages != null && newImages.length > 0) {
            if (restaurant.getImages() != null && !restaurant.getImages().isEmpty()) {
                imageUploadService.deleteExistingRestaurantImages(restaurant);
            }

            Set<RestaurantImage> images = new HashSet<>();
            List<String> imageUrls = imageUploadService.uploadRestaurantImages(restaurant.getId(), newImages);
            for (String imageUrl: imageUrls) {
                images.add(new RestaurantImage(imageUrl, restaurant));
            }
            restaurant.setImages(images);
            restaurantImageRepository.saveAll(images);
        }

        if (newMenuImages != null && !newMenuImages.isEmpty()) {
            for (Map.Entry<Long, MultipartFile> entry: newMenuImages.entrySet()) {
                Long menuId = entry.getKey();
                MultipartFile file = entry.getValue();

                Menu menu = restaurant.getMenus().stream()
                        .filter(m -> m.getId().equals(menuId))
                        .findFirst()
                        .orElseThrow(() -> new EntityNotFoundException("Menu not found"));

                if (menu.getImageUrl() != null) {
                    imageUploadService.deleteMenuImage(restaurant.getId(), menu.getId(), menu.getImageUrl());
                }

                String imageUrl = imageUploadService.uploadMenuImage(restaurant.getId(), menuId, file);
                menu.setImageUrl(imageUrl);
            }
        }

        return restaurantRepository.save(restaurant);
    }

    // 이미지 업로드
    // TODO: 보류
//    public void assignImages(Restaurant restaurant, List<String> imageUrls) {
//        Set<RestaurantImage> images = imageUrls.stream()
//                .map(imageUrl -> RestaurantImage.builder()
//                        .imageUrl(imageUrl)
//                        .restaurant(restaurant)
//                        .build())
//                .collect(Collectors.toSet());
//
//        restaurantImageRepository.saveAll(images);
//    }

    // 카테고리 설정
    private Category assignCategory(String categoryType) {
        if (categoryType == null || categoryType.trim().isEmpty()) {
            throw new RestaurantException("Category must be provided");
        }

        try {
            CategoryType type = CategoryType.valueOf(categoryType.toUpperCase());
            return categoryRepository.findByCategoryType(type)
                    .orElseThrow(() -> new EntityNotFoundException("Category not found"));
        } catch (IllegalArgumentException e) {
            throw new RestaurantException("Invalid category type provided: " + categoryType);
        }
    }

    // 영업시간 설정
    private List<OperatingHours> assignOperatingHours(List<OperatingHoursDto> operatingHoursDto,
                                                      Restaurant restaurant) {
        return operatingHoursDto.stream()
                .map(dto -> OperatingHours.builder()
                        .dayOfWeek(dto.getDayOfWeek())
                        .openingTime(dto.getOpeningTime())
                        .closingTime(dto.getClosingTime())
                        .restaurant(restaurant)
                        .build())
                .collect(Collectors.toList());
    }

    private String getClosedDays(List<OperatingHours> operatingHours) {
        Set<DayOfWeek> allDaysOfWeek = EnumSet.allOf(DayOfWeek.class);

        Set<DayOfWeek> closedDaysSet = new HashSet<>(allDaysOfWeek);
        for (OperatingHours operatingHour: operatingHours) {
            closedDaysSet.remove(operatingHour.getDayOfWeek());
        }

        return closedDaysSet.stream()
                .map(DayOfWeek::toString)
                .collect(Collectors.joining(", "));
    }

    // 메뉴 설정
    private List<Menu> assignMenu(List<MenuRegisterDto> menuDto, Restaurant restaurant) {
        return menuDto.stream()
                .map(dto -> Menu.builder()
                        .name(dto.getName())
                        .price(dto.getPrice())
                        .description(dto.getDescription())
                        .restaurant(restaurant)
                        .build())
                .collect(Collectors.toList());
    }

    // 편의시설 설정
    private List<Facility> assignFacilities(List<String> facilityTypes) {
        return facilityTypes.stream()
                .map(type -> FacilityType.valueOf(type))
                .map(facilityType -> facilityRepository.findByFacilityType(facilityType)
                        .orElseThrow(() -> new EntityNotFoundException("Facility not found")))
                .collect(Collectors.toList());
    }

    // 해시태그 설정
    private List<Hashtag> assignHashtags(List<String> hashtags) {
        return hashtags.stream()
                .map(tag -> hashtagRepository.findByName(tag)
                        .orElseGet(() -> {
                            Hashtag newHashtag = new Hashtag(tag);
                            return hashtagRepository.save(newHashtag);
                        }))
                .collect(Collectors.toList());
    }

    /**
     * 검색
     */
    // 모든 식당 리스트 검색
    public List<RestaurantListDto> getRestaurants(Pageable pageable) {
        Page<Restaurant> restaurants = restaurantRepository.findAll(pageable);

        return restaurants.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 이름으로 식당 검색
    public List<RestaurantListDto> getRestaurantsByName(String name, Pageable pageable) {
        Page<Restaurant> restaurants = restaurantRepository.findByNameContainingIgnoreCase(name, pageable);

        return restaurants.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 업종으로 식당 검색
    public List<RestaurantListDto> getRestaurantsByCategory(String category, Pageable pageable) {
        CategoryType categoryType = convertToCategoryType(category);

        Optional<Category> optionalCategory = categoryRepository.findByCategoryType(categoryType);

        optionalCategory.ifPresent(categoryEntity -> {
            categoryEntity.setSearchCount(categoryEntity.getSearchCount() + 1);
            categoryRepository.save(categoryEntity);
        });

        Category categoryEntity = optionalCategory.orElseThrow(() ->
                new EntityNotFoundException("Category not found"));

        Page<Restaurant> restaurants = restaurantRepository.findByCategory_CategoryType(categoryType, pageable);

        return restaurants.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 메뉴로 식당 검색
    public List<RestaurantListDto> getRestaurantsByMenu(String menu, Pageable pageable) {
        Page<Restaurant> restaurants = restaurantRepository.findByMenus_NameContaining(menu, pageable);

        return restaurants.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 해시태그로 식당 검색
    public List<RestaurantListDto> getRestaurantsByHashtag(String hashtag, Pageable pageable) {
        Optional<Hashtag> optionalHashtag = hashtagRepository.findByName(hashtag);

        optionalHashtag.ifPresent(hashtagEntity -> {
            hashtagEntity.setSearchCount(hashtagEntity.getSearchCount() + 1);
            hashtagRepository.save(hashtagEntity);
        });

//        Hashtag hashtagEntity = optionalHashtag.orElseThrow(() -> (
//            new EntityNotFoundException("Hashtag not found")));

        Page<Restaurant> restaurants = restaurantRepository.findByHashtags_Name(hashtag, pageable);

        return restaurants.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 해시태그 자동완성
    public List<String> getHashtagSuggestions(String hashtag) {
        List<Hashtag> hashtags = hashtagRepository.findTop10ByNameStartingWithIgnoreCase(hashtag);

        return hashtags.stream()
                .map(Hashtag::getName)
                .collect(Collectors.toList());
    }

    // Restaurant -> dto로 변환하는 헬퍼 메서드
    private RestaurantListDto convertToDto(Restaurant restaurant) {
        return new RestaurantListDto(
                restaurant.getId(),
                restaurant.getName(),
                restaurant.getAddress(),
                restaurant.getCategory() != null ? restaurant.getCategory().getCategoryType().name() : null,
                restaurant.getAverageRating()
        );
    }

    // category String -> categoryType enum 으로 변환하는 헬퍼 메서드
    private CategoryType convertToCategoryType(String category) {
        if (category == null) {
            throw new IllegalArgumentException("Category must not be null");
        }

        try {
            return CategoryType.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid category type: " + category);
        }
    }

    // 특정 편의시설을 가진 식당 조회
    public List<Restaurant> getRestaurantsByFacility(FacilityType facilityType, Pageable pageable) {
        // 'EVENT_SPACE' 시설을 가진 Facility를 가진 식당들을 찾아 반환
        return restaurantRepository.findByFacilities_FacilityType(facilityType, pageable);
    }

    // 식당 상세정보 조회
    public RestaurantInfoDto getRestaurantById(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found"));

        // 카테고리
        String category = restaurant.getCategory() != null ?
                restaurant.getCategory().getCategoryType().name() : null;

        // 이미지
        List<String> images = restaurant.getImages().stream()
                .map(RestaurantImage::getImageUrl)
                .collect(Collectors.toList());

        // 메뉴
        List<MenuDetailDto> menus = restaurant.getMenus().stream()
                .map(menu -> new MenuDetailDto(
                        menu.getId(),
                        menu.getName(),
                        menu.getPrice(),
                        menu.getDescription(),
                        menu.getImageUrl(),
                        menu.getRestaurant().getId()))
                .collect(Collectors.toList());

        // 영업시간
        List<OperatingHoursDto> operatingHours = restaurant.getOperatingHours().stream()
                .map(hours -> new OperatingHoursDto(
                        hours.getId(),
                        hours.getDayOfWeek(),
                        hours.getOpeningTime(),
                        hours.getClosingTime(),
                        hours.getRestaurant().getId()))
                .collect(Collectors.toList());

        // 편의시설
        List<String> facilities = restaurant.getFacilities().stream()
                .map(facility -> facility.getFacilityType().name())
                .collect(Collectors.toList());

        // 해시태그
        List<String> hashtags = restaurant.getHashtags().stream()
                .map(Hashtag::getName)
                .collect(Collectors.toList());

        // 공지
        List<AnnouncementDetailDto> announcements = restaurant.getAnnouncements().stream()
                .map(announcement -> new AnnouncementDetailDto(
                        announcement.getId(),
                        announcement.getTitle(),
                        announcement.getContent(),
                        announcement.isEvent(),
                        announcement.getRestaurant().getId(),
                        announcement.getCreatedAt(),
                        announcement.getUpdatedAt()))
                .collect(Collectors.toList());

        // 리뷰
        List<ReviewInfoDto> reviews = restaurant.getReviews().stream()
                .map(review -> new ReviewInfoDto(
                        review.getId(),
                        review.getContent(),
                        review.getRating(),
                        review.getImages().stream()
                                .map(ReviewImage::getImageUrl)
                                .collect(Collectors.toList()),
                        review.getCreatedAt(),
                        review.getUpdatedAt(),
                        review.getRestaurant().getId(),
                        review.getMember().getId(),
                        review.getMember().getProfileImage(),
                        review.getMember().getNickname()))
                .collect(Collectors.toList());

        return RestaurantInfoDto.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .description(restaurant.getDescription())
                .address(restaurant.getAddress())
                .latitude(restaurant.getLatitude())
                .longitude(restaurant.getLongitude())
                .contact(restaurant.getContact())
                .closedDay(restaurant.getClosedDay())
                .category(category)
                .link(restaurant.getLink())
                .info(restaurant.getInfo())
                .deposit(restaurant.getDeposit())
                .images(images)
                .menus(menus)
                .facilities(facilities)
                .hashtags(hashtags)
                .operatingHours(operatingHours)
                .averageRating(restaurant.getAverageRating())
                .reviews(reviews)
                .announcements(announcements)
                .build();
    }


    /**
     * 특정 식당의 모든 채팅방 조회
     */
    public Page<ChatRoomCreateResDto> getChatRoomsByRestaurantId(Long restaurantId, Pageable pageable) {
        return chatRoomRepository.findByRestaurantId(restaurantId, pageable)
                .map(ChatRoomCreateResDto::fromEntity);
    }

}
