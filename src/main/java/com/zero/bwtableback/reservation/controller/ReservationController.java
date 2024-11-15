package com.zero.bwtableback.reservation.controller;

import com.zero.bwtableback.chat.service.ChatService;
import com.zero.bwtableback.payment.PaymentService;
import com.zero.bwtableback.reservation.dto.*;
import com.zero.bwtableback.reservation.service.ReservationService;
import com.zero.bwtableback.security.MemberDetails;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final ChatService chatService;
    private final PaymentService paymentService;

    private final RedissonClient redissonClient;
    private final RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/{reservationId}")
    public ReservationResDto getReservationById(@PathVariable Long reservationId) {
        return reservationService.getReservationById(reservationId);
    }

    /**
     * 예약 생성 요청을 처리
     * 1. 분산 락을 사용하여 동시 접근 제어
     * 2. 예약 가능 여부를 확인
     * 3. 가능한 경우, 임시 예약 정보를 Redis에 저장
     */
    @PostMapping()
    public ResponseEntity<?> createReservation(@RequestBody ReservationCreateReqDto request) {
        String lockKey = "lock:reservation:" + request.restaurantId() + ":" + request.reservationDate() + ":" + request.reservationTime();
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 락 획득 시도
            if (lock.tryLock(5, TimeUnit.SECONDS)) {
                try {
                    // 현재 예약 상태 확인
                    boolean isAvailable = reservationService.checkReservationAvailability(request);
                    if (isAvailable) {
                        // 임시 예약 정보 Redis에 저장
                        String reservationToken = UUID.randomUUID().toString(); // 임시 예약에 대한 고유 아이디 생성
                        redisTemplate.opsForValue().set("reservation:token:" + reservationToken, request, 5, TimeUnit.MINUTES);
                        return ResponseEntity.ok(reservationToken);
                    } else {
                        return ResponseEntity.status(HttpStatus.CONFLICT).body("해당 시간대는 이미 예약이 가득 찼습니다.");
                    }
                } finally {
                    lock.unlock();
                }
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("다른 예약 처리 중입니다. 잠시 후 다시 시도해주세요.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("예약 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * 예약 확정 및 결제 완료
     *
     * @return 결제 완료 페이지에 보여질 정보 반환
     */
    @PostMapping("/complete")
    public ResponseEntity<?> completeReservation(@RequestBody PaymentDto paymentDto,
                                                 @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        String email = memberDetails.getUsername();

        if (paymentService.verifyPayment(paymentDto)) { // 아임포트 결제 검증
            ReservationCompleteResDto response = reservationService.saveReservation(paymentDto, email);

            redisTemplate.delete("reservation:token:" + paymentDto.getReservationToken());
            // TODO 예약 확정으로 변경

            // TODO 채팅방 생성
            chatService.createChatRoom(response.getReservation());

            // TODO 예약 확정 알림
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body("결제 확인에 실패했습니다.");
        }
    }

//    @PostMapping("/complete-payment")
//    public ResponseEntity<PaymentCompleteResDto> confirmReservation(
//            @RequestBody PaymentDto paymentDto) {
//        // FIXME 원래는 저장된 세션의 예약 정보를 가졍옴
//        // TODO 결제 시 PAYMENT 정보 저장
//        // TODO 세션에 있는 예약 정보 저장
//
////        ReservationResponseDto reservationResponseDto = reservationService.confirmReservation(reservationId);
//
//        // FIXME 임의의 예약 정보 생성 - 세션으로 대체
//        Long reservationId = 4L;
//        Long restaurantId = 8L;  // 식당 ID
//        Long memberId = 30L;      // 회원 ID
//        LocalDate reservationDate = LocalDate.of(2024, 11, 15); // 예약 날짜
//        LocalTime reservationTime = LocalTime.of(19, 30); // 예약 시간 (예: 19:30)
//        int numberOfPeople = 4;   // 인원 수
//        String specialRequest = "창가 자리 요청"; // 특별 요청
//        ReservationStatus reservationStatus = ReservationStatus.CONFIRMED; // 예약 상태
//
//        // DTO 객체 생성
//        ReservationResDto reservationResDto = new ReservationResDto(
//                reservationId,
//                restaurantId,
//                memberId,
//                reservationDate,
//                reservationTime,
//                numberOfPeople,
//                specialRequest,
//                reservationStatus
//        );
//
//        PaymentCompleteResDto paymentCompleteDto = chatService.createChatRoom(reservationResDto);
//
//        return ResponseEntity.status(HttpStatus.CREATED).body(paymentCompleteDto);
//    }

    @PutMapping("/{reservationId}/confirm")
    public PaymentCompleteResDto confirmReservation(
            @PathVariable Long reservationId,
            @RequestParam Long restaurantId,
            @AuthenticationPrincipal MemberDetails memberDetails) {
        return reservationService.confirmReservation(reservationId, restaurantId, memberDetails.getMemberId());
    }

    @PutMapping("/{reservationId}/status")
    public ReservationResDto updateReservationStatus(
            @PathVariable Long reservationId,
            @RequestBody ReservationUpdateReqDto statusUpdateDto,
            @AuthenticationPrincipal MemberDetails memberDetails) {
        return reservationService.updateReservationStatus(statusUpdateDto, reservationId, memberDetails.getMemberId());
    }

}
