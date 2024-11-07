package com.zero.bwtableback.restaurant.service;

import com.zero.bwtableback.restaurant.dto.AnnouncementDetailDto;
import com.zero.bwtableback.restaurant.dto.AnnouncementReqDto;
import com.zero.bwtableback.restaurant.dto.AnnouncementResDto;
import com.zero.bwtableback.restaurant.dto.AnnouncementUpdateReqDto;
import com.zero.bwtableback.restaurant.entity.Announcement;
import com.zero.bwtableback.restaurant.entity.Restaurant;
import com.zero.bwtableback.restaurant.repository.AnnouncementRepository;
import com.zero.bwtableback.restaurant.repository.RestaurantRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final RestaurantRepository restaurantRepository;

    // 공지 생성
    public AnnouncementResDto createAnnouncement(AnnouncementReqDto reqDto) {
        Restaurant restaurant = restaurantRepository.findById(reqDto.getRestaurantId())
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found"));

        Announcement announcement = Announcement.builder()
                .title(reqDto.getTitle())
                .content(reqDto.getContent())
                .event(reqDto.isEvent())
                .restaurant(restaurant)
                .build();

        Announcement savedAnnouncement = announcementRepository.save(announcement);

        AnnouncementResDto resDto = new AnnouncementResDto(
                savedAnnouncement.getId(),
                "Announcement created successfully",
                reqDto.getRestaurantId()
        );

        return resDto;
    }

    // 공지 수정
    public AnnouncementResDto updateAnnouncement(Long id, AnnouncementUpdateReqDto reqDto) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Announcement not found"));

        Announcement updatedAnnouncement = announcement.toBuilder()
                .title(reqDto.getTitle() != null ? reqDto.getTitle() : announcement.getTitle())
                .content(reqDto.getContent() != null ? reqDto.getContent() : announcement.getContent())
                .event(reqDto.getEvent() != null ? reqDto.getEvent() : announcement.isEvent())
                .build();

        Announcement savedAnnouncement = announcementRepository.save(updatedAnnouncement);

        AnnouncementResDto resDto = new AnnouncementResDto(
                savedAnnouncement.getId(),
                "Announcement updated successfully",
                savedAnnouncement.getRestaurant().getId()
        );
        return resDto;
    }

    // 공지 삭제
    public void deleteAnnouncement(Long id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Announcement not found"));

        announcementRepository.delete(announcement);
    }

    // 공지 상세 조회
    public AnnouncementDetailDto getAnnouncementById(Long id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Announcement not found"));

        return new AnnouncementDetailDto(
                announcement.getId(),
                announcement.getTitle(),
                announcement.getContent(),
                announcement.isEvent(),
                announcement.getRestaurant().getId(),
                announcement.getCreatedAt(),
                announcement.getUpdatedAt()
        );
    }

    // 식당 공지 목록 조회
    public List<AnnouncementDetailDto> getAnnouncementsByRestaurantId(Long restaurantId, Pageable pageable) {
        Page<Announcement> announcements = announcementRepository.findByRestaurantId(restaurantId, pageable);

        return announcements.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Announcement -> dto로 변환하는 헬퍼 메서드
    private AnnouncementDetailDto convertToDto(Announcement announcement) {
        return new AnnouncementDetailDto(
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
