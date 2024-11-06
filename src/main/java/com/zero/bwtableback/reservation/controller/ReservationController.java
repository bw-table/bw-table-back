package com.zero.bwtableback.reservation.controller;

import com.zero.bwtableback.reservation.dto.ReservationResponseDto;
import com.zero.bwtableback.reservation.entity.ReservationStatus;
import com.zero.bwtableback.reservation.service.ReservationService;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    @GetMapping
    public Page<ReservationResponseDto> getReservations(
            @RequestParam(required = false) Long restaurantId,
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) ReservationStatus reservationStatus,
            @RequestParam(required = false) LocalDate reservationDate,
            @RequestParam(required = false) LocalTime reservationTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return reservationService.findReservationsWithFilters(
                restaurantId, memberId, reservationStatus, reservationDate, reservationTime, pageable);
    }

    @GetMapping("/{reservationId}")
    public ReservationResponseDto getReservationById(@PathVariable Long reservationId) {
        return reservationService.getReservationById(reservationId);
    }

    // TODO: restaurantService 기능이 완성되면 주석 해제
//    // 새로운 예약 추가
//    @PostMapping
//    public ReservationResponseDto createReservation(
//            @RequestBody ReservationRequestDto reservationRequestDto,
//            @RequestParam Long restaurantId,
//            @AuthenticationPrincipal PrincipalDetails principalDetails) {
//
//        Restaurant restaurant = restaurantService.findRestaurantById(restaurantId);
//        Member member = principalDetails.getMember();
//
//        return reservationService.createReservation(reservationRequestDto, restaurant, member);
//    }

    @PutMapping("/{reservationId}/confirm")
    public ReservationResponseDto confirmReservation(@PathVariable Long reservationId) {
        return reservationService.confirmReservation(reservationId);
    }

    @PutMapping("/{reservationId}/cancel/customer")
    public ReservationResponseDto cancelReservationByCustomer(@PathVariable Long reservationId) {
        return reservationService.cancelReservationByCustomer(reservationId);
    }

    @PutMapping("/{reservationId}/cancel/owner")
    public ReservationResponseDto cancelReservationByOwner(@PathVariable Long reservationId) {
        return reservationService.cancelReservationByOwner(reservationId);
    }

    @PutMapping("/{reservationId}/no-show")
    public ReservationResponseDto markReservationAsNoShow(@PathVariable Long reservationId) {
        return reservationService.markReservationAsNoShow(reservationId);
    }

    @PutMapping("/{reservationId}/visited")
    public ReservationResponseDto markReservationAsVisited(@PathVariable Long reservationId) {
        return reservationService.markReservationAsVisited(reservationId);
    }

}
