package com.zero.bwtableback.restaurant.controller;

import com.zero.bwtableback.restaurant.dto.AnnouncementDetailDto;
import com.zero.bwtableback.restaurant.dto.AnnouncementReqDto;
import com.zero.bwtableback.restaurant.dto.AnnouncementResDto;
import com.zero.bwtableback.restaurant.dto.AnnouncementUpdateReqDto;
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

    // 공지 수정
    @PutMapping("/{announcementId}")
    public ResponseEntity<AnnouncementResDto> updateAnnouncement(@PathVariable Long announcementId,
                                                                    @RequestBody AnnouncementUpdateReqDto reqDto) {
        AnnouncementResDto resDto = announcementService.updateAnnouncement(announcementId, reqDto);

        return ResponseEntity.ok(resDto);

    }

    // 공지 삭제
    @DeleteMapping("/{announcementId}")
    public ResponseEntity<String> deleteAnnouncement(@PathVariable Long announcementId) {

        announcementService.deleteAnnouncement(announcementId);
        return ResponseEntity.ok("Announcement deleted successfully");
    }

    // 공지 상세 조회
    @GetMapping("/{announcementId}")
    public ResponseEntity<AnnouncementDetailDto> getAnnouncementById(@PathVariable Long announcementId) {
        AnnouncementDetailDto detailDto = announcementService.getAnnouncementById(announcementId);

        return ResponseEntity.ok(detailDto);
    }

}
