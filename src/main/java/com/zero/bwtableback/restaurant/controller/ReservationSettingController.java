package com.zero.bwtableback.restaurant.controller;

import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.restaurant.dto.ReservationSettingDetailDto;
import com.zero.bwtableback.restaurant.dto.ReservationSettingReqDto;
import com.zero.bwtableback.restaurant.dto.ReservationSettingResDto;
import com.zero.bwtableback.restaurant.service.ReservationSettingService;
import com.zero.bwtableback.security.MemberDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/restaurants")
public class ReservationSettingController {

    private final ReservationSettingService reservationSettingService;

    @PostMapping("/{restaurantId}/reservation-settings") // TODO 엔드포인트 수정
    public ResponseEntity<ReservationSettingResDto> createReservationSetting(
            @PathVariable Long restaurantId,
            @RequestBody ReservationSettingReqDto reqDto,
            @AuthenticationPrincipal MemberDetails memberDetails) throws AccessDeniedException {

        Member member = memberDetails.getMember();
        reqDto.setRestaurantId(restaurantId);

        ReservationSettingResDto resDto = reservationSettingService.createReservationSetting(reqDto, member);
        return ResponseEntity.ok(resDto);
    }

    // 특정 식당의 모든 예약 설정 조회
    @GetMapping("/{restaurantId}/reservation-settings")
    public ResponseEntity<List<ReservationSettingDetailDto>> getReservationSettingsByRestaurantId(
            @PathVariable Long restaurantId,
            @AuthenticationPrincipal MemberDetails memberDetails) throws AccessDeniedException {

        Member member = memberDetails.getMember();
        List<ReservationSettingDetailDto> reservationSettings =
                reservationSettingService.getReservationSettingByRestaurantId(restaurantId, member);

        return ResponseEntity.ok(reservationSettings);
    }

    // 특정 예약 설정 조회
    @GetMapping("/{restaurantId}/reservation-settings/{reservationSettingId}")
    public ResponseEntity<ReservationSettingDetailDto> getReservationSettingById(
                                                                @PathVariable Long restaurantId,
                                                                @PathVariable Long reservationSettingId,
                                                                @AuthenticationPrincipal MemberDetails memberDetails) throws AccessDeniedException {

        Member member = memberDetails.getMember();

        ReservationSettingDetailDto resDto = reservationSettingService.getReservationSettingById(reservationSettingId, restaurantId, member);
        return ResponseEntity.ok(resDto);
    }

    // 특정 예약 설정 삭제
    @DeleteMapping("/{restaurantId}/reservation-settings/{reservationSettingId}")
    public ResponseEntity<ReservationSettingResDto> deleteReservationSetting(
            @PathVariable Long restaurantId,
            @PathVariable Long reservationSettingId,
            @AuthenticationPrincipal MemberDetails memberDetails) throws AccessDeniedException {

        Member member = memberDetails.getMember();

        reservationSettingService.deleteReservationSetting(reservationSettingId, restaurantId, member);

        ReservationSettingResDto resDto = ReservationSettingResDto.builder()
                .id(reservationSettingId)
                .restaurantId(restaurantId)
                .message("ReservationSetting deleted successfully")
                .build();

        return ResponseEntity.ok(resDto);
    }

}
