package com.zero.bwtableback.restaurant.service;

import com.zero.bwtableback.restaurant.dto.AnnouncementInfoDto;
import com.zero.bwtableback.restaurant.dto.AnnouncementResDto;
import com.zero.bwtableback.restaurant.entity.Announcement;
import com.zero.bwtableback.restaurant.entity.Restaurant;
import com.zero.bwtableback.restaurant.repository.AnnouncementRepository;
import com.zero.bwtableback.restaurant.repository.RestaurantRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final RestaurantRepository restaurantRepository;

    // 공지 생성
    public AnnouncementResDto createAnnouncement(Long restaurantId, String title, String content, boolean isEvent) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found"));

        Announcement announcement = Announcement.builder()
                .title(title)
                .content(content)
                .event(isEvent)
                .restaurant(restaurant)
                .build();

        Announcement savedAnnouncement = announcementRepository.save(announcement);

        AnnouncementResDto resDto = new AnnouncementResDto(
                savedAnnouncement.getId(),
                "Announcement created successfully",
                restaurantId

        );

        return resDto;
    }

    // 공지 상세 조회
    public AnnouncementInfoDto getAnnouncementById(Long id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Announcement not found"));

        return new AnnouncementInfoDto(
                announcement.getId(),
                announcement.getTitle(),
                announcement.getContent(),
                announcement.isEvent(),
                announcement.getRestaurant().getId(),
                announcement.getCreatedAt(),
                announcement.getUpdatedAt()
        );
    }

}
