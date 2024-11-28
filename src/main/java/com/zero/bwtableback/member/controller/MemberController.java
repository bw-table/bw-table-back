package com.zero.bwtableback.member.controller;

import com.zero.bwtableback.chat.dto.ChatRoomCreateResDto;
import com.zero.bwtableback.common.service.ImageUploadService;
import com.zero.bwtableback.member.dto.MemberDto;
import com.zero.bwtableback.member.service.MemberService;
import com.zero.bwtableback.reservation.dto.ReservationResDto;
import com.zero.bwtableback.restaurant.dto.ReviewDetailDto;
import com.zero.bwtableback.security.MemberDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;
    private final ImageUploadService imageUploadService;

    /**
     * 회원 목록 조회
     */
    @GetMapping
    public ResponseEntity<Page<MemberDto>> getMembers(Pageable pageable) {
        return ResponseEntity.ok(memberService.getMembers(pageable));
    }

    /**
     * 특정 회원 정보 조회
     */
    @GetMapping("/{memberId}")
    public MemberDto getMemberById(@PathVariable Long memberId) {
        return memberService.getMemberById(memberId);
    }

    /**
     * 본인 정보 조회
     *
     * JWT 토큰으로 현재 사용자 인증
     */
    @GetMapping("/me")
    public ResponseEntity<?> getMyInfo(@AuthenticationPrincipal MemberDetails memberDetails) {
        if (memberDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long memberId = memberDetails.getMemberId();
        return ResponseEntity.ok(memberService.getMyInfo(memberId));
    }

    /**
     * 나의 모든 예약 조회
     */
    @GetMapping("/me/reservations")
    public ResponseEntity<Page<ReservationResDto>> getMyReservations(Pageable pageable,
                                                                     @AuthenticationPrincipal MemberDetails memberDetails) {
        if (memberDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String email = memberDetails.getUsername();

        return ResponseEntity.ok(memberService.getMyReservations(pageable, email));
    }

    /**
     * 나의 모든 리뷰 조회
     */
    @GetMapping("/me/reviews")
    public ResponseEntity<Page<ReviewDetailDto>> getMyReviews(Pageable pageable,
                                                              @AuthenticationPrincipal MemberDetails memberDetails) {
        String email = memberDetails.getUsername();
        return ResponseEntity.ok(memberService.getMyReviews(pageable, email));
    }

    /**
     * 나의 모든 채팅방 조회
     */
    @GetMapping("/me/chats")
    public ResponseEntity<Page<ChatRoomCreateResDto>> getMyChats(Pageable pageable,
                                                                 @AuthenticationPrincipal MemberDetails memberDetails) {
        if (memberDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String email = memberDetails.getUsername();

        Page<ChatRoomCreateResDto> rooms = memberService.getMyChatRooms(pageable, email);

        return ResponseEntity.ok(rooms);
    }

    /**
     * 프로필 이미지 업로드 (S3)
     */
    @PostMapping("/me/profile-image")
    public ResponseEntity<Map<String, String>> uploadFile(@AuthenticationPrincipal MemberDetails memberDetails,
                                                          @RequestParam("file") MultipartFile file) {
        if (memberDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String email = memberDetails.getUsername();

        try {
            String fileUrl = imageUploadService.uploadProfileImage(file, memberDetails.getMemberId());

            Map<String, String> response = new HashMap<>();
            response.put("imageUrl", fileUrl);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 이미지 수정
     * 기존 이미지 삭제 (S3)
     * 새로운 이미지 업로드 (S3)
     */
    @PutMapping("/me/profile-image")
    public ResponseEntity<?> updateProfileImage(@AuthenticationPrincipal MemberDetails memberDetails,
                                                @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(imageUploadService.updateProfileImage(file, memberDetails.getMemberId()));
    }

    /**
     * 이미지 삭제
     * S3 이미지 삭제
     * DB에서 삭제
     */
    @DeleteMapping("/me/profile-image")
    public ResponseEntity<?> removeProfileImage(@AuthenticationPrincipal MemberDetails memberDetails) throws IOException {
        imageUploadService.deleteFileFromDB(memberDetails.getMemberId());
        return ResponseEntity.noContent().build();
    }
}
