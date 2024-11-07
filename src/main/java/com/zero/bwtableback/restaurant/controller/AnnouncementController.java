package com.zero.bwtableback.restaurant.controller;

import com.zero.bwtableback.restaurant.dto.AnnouncementInfoDto;
import com.zero.bwtableback.restaurant.dto.AnnouncementReqDto;
import com.zero.bwtableback.restaurant.dto.AnnouncementResDto;
import com.zero.bwtableback.restaurant.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    // 공지 생성
    @PostMapping("/new")
    public ResponseEntity<AnnouncementResDto> createAnnouncement(@RequestBody AnnouncementReqDto reqDto) {
        AnnouncementResDto resDto = announcementService.createAnnouncement(
                reqDto.getRestaurantId(),
                reqDto.getTitle(),
                reqDto.getContent(),
                reqDto.isEvent()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(resDto);
    }

    // 공지 상세 조회
    @GetMapping("/{announcementId}")
    public ResponseEntity<AnnouncementInfoDto> getAnnouncementById(@PathVariable Long announcementId) {
        AnnouncementInfoDto infoDto = announcementService.getAnnouncementById(announcementId);

        return ResponseEntity.ok(infoDto);
    }

}
