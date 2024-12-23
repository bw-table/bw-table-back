package com.zero.bwtableback.restaurant.service;

import com.zero.bwtableback.chat.dto.ChatRoomCreateResDto;
import com.zero.bwtableback.chat.repository.ChatRoomRepository;
import com.zero.bwtableback.common.exception.CustomException;
import com.zero.bwtableback.common.exception.ErrorCode;
import com.zero.bwtableback.common.service.ImageUploadService;
import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.member.repository.MemberRepository;
import com.zero.bwtableback.restaurant.dto.*;
import com.zero.bwtableback.restaurant.entity.*;
import com.zero.bwtableback.restaurant.exception.RestaurantException;
import com.zero.bwtableback.restaurant.repository.*;
import com.zero.bwtableback.security.MemberDetails;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
    public RestaurantRegisterResDto registerRestaurant(RestaurantReqDto reqDto,
                                                       MultipartFile[] images,
                                                       List<MenuRegisterDto> menus,
                                                       List<MultipartFile> menuImages,
                                                       Long memberId) throws IOException {

        if (restaurantRepository.existsByAddress(reqDto.getAddress())) {
            throw new RestaurantException("Restaurant with this address already exists.");
        }

        if (restaurantRepository.existsByContact(reqDto.getContact())) {
            throw new RestaurantException("Restaurant with this contact number already exists.");
        }

        Category category = assignCategory(reqDto.getCategory());

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

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
                .category(category)
                .images(new HashSet<>())
                .operatingHours(new ArrayList<>())
                .menus(new ArrayList<>())
                .facilities(new ArrayList<>())
                .hashtags(new ArrayList<>())
                .member(member)
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

            for (String imageUrl : imageUrls) {
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

        return new RestaurantRegisterResDto(
                savedRestaurant.getId(),
                savedRestaurant.getName(),
                "Restaurant registered successfully"
        );
    }

    // 식당 정보 수정
    public RestaurantRegisterResDto updateRestaurant(Long id,
                                                     RestaurantUpdateReqDto reqDto,
                                                     MultipartFile[] newImages,
                                                     List<MultipartFile> newMenuImages,
                                                     Long memberId) throws IOException {

        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found"));

        if (!restaurant.getMember().getId().equals(memberId)) {
            throw new AccessDeniedException("You can only update your own restaurant");
        }

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

        if (reqDto.getOperatingHours() != null) {
            List<OperatingHours> operatingHours = assignOperatingHours(reqDto.getOperatingHours(), restaurant);
            restaurant.setOperatingHours(operatingHours);
        }

        updateMenu(restaurant, reqDto, newMenuImages);

        // 삭제할 이미지 처리
        if (reqDto.getImageIdsToDelete() != null && !reqDto.getImageIdsToDelete().isEmpty()) {
            for (Long imageId : reqDto.getImageIdsToDelete()) {
                imageUploadService.deleteRestaurantImage(imageId);
            }
        }

        // 새로운 이미지 처리
        if (newImages != null && newImages.length > 0) {
            Set<RestaurantImage> images = new HashSet<>();

            List<String> imageUrls = imageUploadService.uploadRestaurantImages(restaurant.getId(), newImages);
            for (String imageUrl : imageUrls) {
                images.add(new RestaurantImage(imageUrl, restaurant));
            }
            restaurant.setImages(images);
            restaurantImageRepository.saveAll(images);
        }

        Restaurant updatedRestaurant = restaurantRepository.save(restaurant);
        return new RestaurantRegisterResDto(
                updatedRestaurant.getId(),
                updatedRestaurant.getName(),
                "Restaurant updated successfully"
        );
    }

    // 메뉴 수정
    public void updateMenu(Restaurant restaurant, RestaurantUpdateReqDto reqDto, List<MultipartFile> menuImages) throws IOException {
        if (reqDto.getMenus() != null && reqDto.getMenus().size() > 0) {
            for (int i = 0; i < reqDto.getMenus().size(); i++) {
                MenuUpdateDto menuDto = reqDto.getMenus().get(i);
                Menu menu = restaurant.getMenus().stream()
                        .filter(m -> m.getId().equals(menuDto.getId()))
                        .findFirst()
                        .orElseThrow(() -> new EntityNotFoundException("Menu not found"));

                Menu updatedMenu = menu;

                // 메뉴 정보 수정
                if (menuDto.getName() != null && !menuDto.getName().equals(menu.getName())) {
                    updatedMenu = updatedMenu.toBuilder().name(menuDto.getName()).build();
                }

                if (menuDto.getPrice() != null && !menuDto.getPrice().equals(menu.getPrice())) {
                    updatedMenu = updatedMenu.toBuilder().price(menuDto.getPrice()).build();
                }

                if (menuDto.getDescription() != null && !menuDto.getDescription().equals(menu.getDescription())) {
                    updatedMenu = updatedMenu.toBuilder().description(menuDto.getDescription()).build();
                }

                menuRepository.save(updatedMenu);

                // 기존 메뉴 이미지 삭제
                if (menuDto.getDeleteImage() != null && menuDto.getDeleteImage()) {
                    if (menu.getImageUrl() != null) {
                        imageUploadService.deleteMenuImage(restaurant.getId(), menu.getId());
                        menu.setImageUrl(null);
                        menuRepository.save(menu); // 변경된 내용 저장
                    }
                }

                // 새로운 메뉴 이미지 추가
                if (menuImages != null && !menuImages.isEmpty() && menuImages.size() > i) {
                    MultipartFile menuImage = menuImages.get(i);

                    if (menuImage != null) {
                        // 기존 이미지 삭제
                        if (menu.getImageUrl() != null) {
                            imageUploadService.deleteMenuImage(restaurant.getId(), menu.getId());
                        }

                        // 새 이미지 업로드
                        String newImageUrl = imageUploadService.uploadMenuImage(restaurant.getId(), menu.getId(), menuImage);
                        menu.setImageUrl(newImageUrl);
                        menuRepository.save(menu);
                    }
                }
            }
        }
    }

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
        for (OperatingHours operatingHour : operatingHours) {
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

    // 식당 상세정보 조회
    public RestaurantDetailDto getRestaurantById(Long id) {
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
        List<ReviewDetailDto> reviews = restaurant.getReviews().stream()
                .map(review -> new ReviewDetailDto(
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

        return RestaurantDetailDto.builder()
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