package com.zero.bwtableback.restaurant.controller;

import com.zero.bwtableback.restaurant.dto.ReservationSettingReqDto;
import com.zero.bwtableback.restaurant.dto.ReservationSettingResDto;
import com.zero.bwtableback.restaurant.service.ReservationSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/restaurants")
public class ReservationSettingController {

    private final ReservationSettingService reservationSettingService;

    @PostMapping("/{restaurantId}/reservation-setting") // TODO 엔드포인트 수정
    public ResponseEntity<ReservationSettingResDto> createReservationSetting(
            @RequestBody ReservationSettingReqDto reqDto) {

        ReservationSettingResDto resDto = reservationSettingService.createReservationSetting(reqDto);
        return ResponseEntity.ok(resDto);
    }


}
