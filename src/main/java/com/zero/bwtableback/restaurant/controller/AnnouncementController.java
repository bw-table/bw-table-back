package com.zero.bwtableback.restaurant.controller;

import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.restaurant.dto.AnnouncementDetailDto;
import com.zero.bwtableback.restaurant.dto.AnnouncementReqDto;
import com.zero.bwtableback.restaurant.dto.AnnouncementResDto;
import com.zero.bwtableback.restaurant.dto.AnnouncementUpdateReqDto;
import com.zero.bwtableback.restaurant.service.AnnouncementService;
import com.zero.bwtableback.security.MemberDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    // 공지 생성
    @PreAuthorize("hasrole('OWNER')")
    @PostMapping("/{restaurantId}/announcements")
    public ResponseEntity<AnnouncementResDto> createAnnouncement(@PathVariable Long restaurantId,
                                                                 @RequestBody AnnouncementReqDto reqDto,
                                                                 @AuthenticationPrincipal MemberDetails memberDetails) throws AccessDeniedException {

        Member member = memberDetails.getMember();

        reqDto = reqDto.toBuilder()
                .restaurantId(restaurantId)
                .build();
        System.out.println(reqDto.getRestaurantId());

        AnnouncementResDto resDto = announcementService.createAnnouncement(reqDto, member);

        return ResponseEntity.ok(resDto);
    }

    // 식당 공지 목록 조회
    @GetMapping("/{restaurantId}/announcements")
    public ResponseEntity<List<AnnouncementDetailDto>> getAnnouncementsByRestaurantId(
            @PathVariable Long restaurantId, Pageable pageable) {
        List<AnnouncementDetailDto> announcements = announcementService.getAnnouncementsByRestaurantId(restaurantId, pageable);

        return ResponseEntity.ok(announcements);
    }

    // 공지 수정
    @PreAuthorize("hasrole('OWNER')")
    @PutMapping("/{restaurantId}/announcements/{announcementId}")
    public ResponseEntity<AnnouncementResDto> updateAnnouncement(@PathVariable Long restaurantId,
                                                                 @PathVariable Long announcementId,
                                                                 @RequestBody AnnouncementUpdateReqDto reqDto,
                                                                 @AuthenticationPrincipal MemberDetails memberDetails) throws AccessDeniedException {

        Member member = memberDetails.getMember();

        AnnouncementResDto resDto = announcementService.updateAnnouncement(restaurantId, announcementId, reqDto, member);

        return ResponseEntity.ok(resDto);
    }

    // 공지 삭제
    @PreAuthorize("hasrole('OWNER')")
    @DeleteMapping("{restaurantId}/announcements/{announcementId}")
    public ResponseEntity<String> deleteAnnouncement(@PathVariable Long restaurantId,
                                                     @PathVariable Long announcementId,
                                                     @AuthenticationPrincipal MemberDetails memberDetails) throws AccessDeniedException {
        Member member = memberDetails.getMember();

        announcementService.deleteAnnouncement(restaurantId, announcementId, member);

        return ResponseEntity.ok("Announcement deleted successfully");
    }

    // 공지 상세 조회
    @GetMapping("/{restaurantId}/announcements/{announcementId}")
    public ResponseEntity<AnnouncementDetailDto> getAnnouncementById(@PathVariable Long restaurantId,
                                                                     @PathVariable Long announcementId) throws AccessDeniedException {

        AnnouncementDetailDto detailDto = announcementService.getAnnouncementById(restaurantId, announcementId);

        return ResponseEntity.ok(detailDto);
    }

}
