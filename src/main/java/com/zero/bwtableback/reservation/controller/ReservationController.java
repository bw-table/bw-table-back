package com.zero.bwtableback.reservation.controller;

import com.zero.bwtableback.chat.dto.ChatRoomCreateResDto;
import com.zero.bwtableback.chat.service.ChatService;
import com.zero.bwtableback.reservation.dto.PaymentDto;
import com.zero.bwtableback.reservation.dto.ReservationResponseDto;
import com.zero.bwtableback.reservation.entity.ReservationStatus;
import com.zero.bwtableback.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final ChatService chatService;

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

    /**
     * 예약 확정 및 결제 완료
     */
    @PostMapping("/complete-payment")
    public ResponseEntity<ChatRoomCreateResDto> confirmReservation(
            @RequestBody PaymentDto paymentDto) {
        // FIXME 원래는 저장된 세션의 예약 정보를 가졍옴
        // TODO 결제 시 PAYMENT 정보 저장
        // TODO 세션에 있는 예약 정보 저장

//        ReservationResponseDto reservationResponseDto = reservationService.confirmReservation(reservationId);

        // 임의의 예약 정보 생성
        Long reservationId = 4L;
        Long restaurantId = 8L;  // 식당 ID
        Long memberId = 30L;      // 회원 ID
        LocalDate reservationDate = LocalDate.of(2024, 11, 15); // 예약 날짜
        LocalTime reservationTime = LocalTime.of(19, 30); // 예약 시간 (예: 19:30)
        int numberOfPeople = 4;   // 인원 수
        String specialRequest = "창가 자리 요청"; // 특별 요청
        ReservationStatus reservationStatus = ReservationStatus.CONFIRMED; // 예약 상태

        // DTO 객체 생성
        ReservationResponseDto reservationResponseDto = new ReservationResponseDto(
                reservationId,
                restaurantId,
                memberId,
                reservationDate,
                reservationTime,
                numberOfPeople,
                specialRequest,
                reservationStatus
        );

        ChatRoomCreateResDto chatRoomCreateResDto = chatService.createChatRoom(reservationResponseDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(chatRoomCreateResDto);
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
