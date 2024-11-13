package com.zero.bwtableback.reservation.controller;

import com.zero.bwtableback.reservation.dto.PaymentCompleteResDto;
import com.zero.bwtableback.reservation.dto.ReservationCreateReqDto;
import com.zero.bwtableback.reservation.dto.ReservationResDto;
import com.zero.bwtableback.reservation.dto.ReservationUpdateReqDto;
import com.zero.bwtableback.chat.dto.ChatRoomCreateResDto;
import com.zero.bwtableback.chat.service.ChatService;
import com.zero.bwtableback.reservation.dto.PaymentCompleteDto;
import com.zero.bwtableback.reservation.dto.PaymentDto;
import com.zero.bwtableback.reservation.dto.ReservationResponseDto;
import com.zero.bwtableback.reservation.entity.ReservationStatus;
import com.zero.bwtableback.reservation.service.ReservationService;
import com.zero.bwtableback.security.MemberDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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

    @GetMapping("/{reservationId}")
    public ReservationResDto getReservationById(@PathVariable Long reservationId) {
        return reservationService.getReservationById(reservationId);
    }

    @PostMapping
    public ReservationResDto createReservation(
            @RequestBody ReservationCreateReqDto reservationCreateReqDto,
            @RequestParam Long restaurantId,
            @AuthenticationPrincipal MemberDetails memberDetails) {
        Long memberId = memberDetails.getMember().getId();
        return reservationService.createReservation(reservationCreateReqDto, restaurantId, memberId);
    }

    /**
     * 예약 확정 및 결제 완료
     */
    @PostMapping("/complete-payment")
    public ResponseEntity<PaymentCompleteDto> confirmReservation(
            @RequestBody PaymentDto paymentDto) {
        // FIXME 원래는 저장된 세션의 예약 정보를 가졍옴
        // TODO 결제 시 PAYMENT 정보 저장
        // TODO 세션에 있는 예약 정보 저장

//        ReservationResponseDto reservationResponseDto = reservationService.confirmReservation(reservationId);

        // FIXME 임의의 예약 정보 생성 - 세션으로 대체
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

        PaymentCompleteDto paymentCompleteDto = chatService.createChatRoom(reservationResponseDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(paymentCompleteDto);
    @PutMapping("/{reservationId}/confirm")
    public PaymentCompleteResDto confirmReservation(
            @PathVariable Long reservationId,
            @RequestParam Long restaurantId) {
        return reservationService.confirmReservation(reservationId, restaurantId);
    }

    @PutMapping("/{reservationId}/status")
    public ReservationResDto updateReservationStatus(
            @PathVariable Long reservationId,
            @RequestBody ReservationUpdateReqDto statusUpdateDto) {
        return reservationService.updateReservationStatus(reservationId, statusUpdateDto);
    }

}
