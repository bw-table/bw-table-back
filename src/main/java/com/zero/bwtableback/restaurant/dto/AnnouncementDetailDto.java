package com.zero.bwtableback.restaurant.dto;

import com.zero.bwtableback.restaurant.entity.Announcement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class AnnouncementDetailDto {

    private Long id;
    private String title;
    private String content;
    private boolean event;
    private Long restaurantId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AnnouncementDetailDto fromEntity(Announcement announcement) {
        return null;
    }
}
