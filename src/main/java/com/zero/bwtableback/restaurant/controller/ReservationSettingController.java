package com.zero.bwtableback.restaurant.controller;

import com.zero.bwtableback.restaurant.dto.ReservationSettingDetailDto;
import com.zero.bwtableback.restaurant.dto.ReservationSettingReqDto;
import com.zero.bwtableback.restaurant.dto.ReservationSettingResDto;
import com.zero.bwtableback.restaurant.service.ReservationSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/restaurants")
public class ReservationSettingController {

    private final ReservationSettingService reservationSettingService;

    @PostMapping("/{restaurantId}/reservation-settings") // TODO 엔드포인트 수정
    public ResponseEntity<ReservationSettingResDto> createReservationSetting(
            @PathVariable Long restaurantId,
            @RequestBody ReservationSettingReqDto reqDto) {

        System.out.println(restaurantId);
        ReservationSettingResDto resDto = reservationSettingService.createReservationSetting(reqDto);
        return ResponseEntity.ok(resDto);
    }

    // 특정 식당의 모든 예약 설정 조회
    @GetMapping("/{restaurantId}/reservation-settings")
    public ResponseEntity<List<ReservationSettingDetailDto>> getReservationSettingsByRestaurantId(
            @PathVariable Long restaurantId) {
        List<ReservationSettingDetailDto> reservationSettings = reservationSettingService.getReservationSettingByRestaurantId(restaurantId);

        return ResponseEntity.ok(reservationSettings);
    }

    // 특정 예약 설정 조회
    @GetMapping("/{restaurantId}/reservation-settings/{id}")
    public ResponseEntity<ReservationSettingDetailDto> getReservationSettingById(
                                                                @PathVariable Long restaurantId,
                                                                @PathVariable Long id) {

        ReservationSettingDetailDto resDto = reservationSettingService.getReservationSettingById(id);
        return ResponseEntity.ok(resDto);
    }

    // 특정 예약 설정 삭제
    @DeleteMapping("/{restaurantId}/reservation-settings/{id}")
    public ResponseEntity<ReservationSettingResDto> deleteReservationSetting(
            @PathVariable Long restaurantId,
            @PathVariable Long id) {
        reservationSettingService.deleteReservationSetting(id);

        ReservationSettingResDto resDto = ReservationSettingResDto.builder()
                .id(id)
                .restaurantId(restaurantId)
                .message("ReservationSetting deleted successfully")
                .build();

        return ResponseEntity.ok(resDto);
    }

}
