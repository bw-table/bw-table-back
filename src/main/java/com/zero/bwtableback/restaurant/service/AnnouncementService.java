package com.zero.bwtableback.restaurant.service;

import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.restaurant.dto.AnnouncementDetailDto;
import com.zero.bwtableback.restaurant.dto.AnnouncementReqDto;
import com.zero.bwtableback.restaurant.dto.AnnouncementResDto;
import com.zero.bwtableback.restaurant.dto.AnnouncementUpdateReqDto;
import com.zero.bwtableback.restaurant.entity.Announcement;
import com.zero.bwtableback.restaurant.entity.Restaurant;
import com.zero.bwtableback.restaurant.repository.AnnouncementRepository;
import com.zero.bwtableback.restaurant.repository.RestaurantRepository;
import com.zero.bwtableback.security.MemberDetails;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final RestaurantRepository restaurantRepository;

    // 공지 생성
    public AnnouncementResDto createAnnouncement(AnnouncementReqDto reqDto, Member member) throws AccessDeniedException {

        Restaurant restaurant = findRestaurant(reqDto.getRestaurantId());

        if (!restaurant.getMember().getId().equals(member.getId())) {
            throw new AccessDeniedException("You can only create announcement for your own restaurant");
        }

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
    public AnnouncementResDto updateAnnouncement(Long restaurantId, Long announcementId, AnnouncementUpdateReqDto reqDto, Member member) throws AccessDeniedException {
        Restaurant restaurant = findRestaurant(restaurantId);
        Announcement announcement = findAnnouncement(announcementId);

        validateOwnerAndAnnouncementBelongsToRestaurant(restaurant, announcement, member);

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
    public void deleteAnnouncement(Long restaurantId, Long announcementId, Member member) throws AccessDeniedException {
        Restaurant restaurant = findRestaurant(restaurantId);
        Announcement announcement = findAnnouncement(announcementId);

        validateOwnerAndAnnouncementBelongsToRestaurant(restaurant, announcement, member);

        announcementRepository.delete(announcement);
    }

    // 공지 상세 조회
    public AnnouncementDetailDto getAnnouncementById(Long restaurantId, Long announcementId) throws AccessDeniedException {
        Restaurant restaurant = findRestaurant(restaurantId);
        Announcement announcement = findAnnouncement(announcementId);

        if (!announcement.getRestaurant().equals(restaurant)) {
            throw new AccessDeniedException("This announcement does not belong to the given restaurant");
        }

        return convertToDto(announcement);
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

    private Restaurant findRestaurant(Long restaurantId) {
        return restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found"));
    }

    private Announcement findAnnouncement(Long announcementId) {
        return announcementRepository.findById(announcementId)
                .orElseThrow(() -> new EntityNotFoundException("Announcement not found"));
    }

    private void validateOwnerAndAnnouncementBelongsToRestaurant(Restaurant restaurant, Announcement announcement, Member member) throws AccessDeniedException {
        if (!restaurant.getMember().getId().equals(member.getId())) {
            throw new AccessDeniedException("You can only access your own announcements");
        }

        if (!announcement.getRestaurant().equals(restaurant)) {
            throw new AccessDeniedException("This announcement does not belong to the given restaurant");
        }
    }
}
