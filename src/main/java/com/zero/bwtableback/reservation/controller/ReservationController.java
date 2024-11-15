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
     *
     * TODO 예외 처리
     */
    @PostMapping()
    public ResponseEntity<?> requestReservation(@RequestBody ReservationCreateReqDto request) {
        boolean isAvailable = reservationService.checkReservationAvailability(request);
        if (isAvailable) {
            String reservationToken = UUID.randomUUID().toString();
            redisTemplate.opsForValue().set("reservation:token:" + reservationToken, request, 5, TimeUnit.MINUTES);
            return ResponseEntity.ok(reservationToken);
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("해당 시간대는 이미 예약이 가득 찼습니다.");
        }
//        String lockKey = "lock:reservation:" + request.restaurantId() + ":" + request.reservationDate() + ":" + request.reservationTime();
//        RLock lock = redissonClient.getLock(lockKey);

//        try {
//            // 락 획득 시도
//            if (lock.tryLock(5, TimeUnit.SECONDS)) {
//                try {
//                    // 현재 예약 상태 확인
//                    boolean isAvailable = reservationService.checkReservationAvailability(request);
//                    if (isAvailable) {
//                        // 임시 예약 정보 Redis에 저장
//                        String reservationToken = UUID.randomUUID().toString(); // 임시 예약에 대한 고유 아이디 생성
//                        redisTemplate.opsForValue().set("reservation:token:" + reservationToken, request, 5, TimeUnit.MINUTES);
//                        return ResponseEntity.ok(reservationToken);
//                    } else {
//                        return ResponseEntity.status(HttpStatus.CONFLICT).body("해당 시간대는 이미 예약이 가득 찼습니다.");
//                    }
//                } finally {
//                    lock.unlock();
//                }
//            } else {
//                return ResponseEntity.status(HttpStatus.CONFLICT).body("다른 예약 처리 중입니다. 잠시 후 다시 시도해주세요.");
//            }
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("예약 처리 중 오류가 발생했습니다.");
//        }
    }

    /**
     * 예약 확정 및 결제 완료
     *
     * @return 결제 완료 페이지에 보여질 정보 반환
     */
    @PostMapping("/complete")
    public ResponseEntity<?> completeReservation(@RequestBody PaymentResDto paymentResDto,
                                                 @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        String reservationKey = "reservation:token:" + paymentResDto.getReservationToken();
        ReservationCreateReqDto reservationInfo = (ReservationCreateReqDto) redisTemplate.opsForValue().get(reservationKey);

        if (paymentService.verifyPaymentAndSave(paymentResDto)) {
            String lockKey = "lock:reservation:" + reservationInfo.restaurantId() + ":" + reservationInfo.reservationDate() + ":" + reservationInfo.reservationTime();
            RLock lock = redissonClient.getLock(lockKey);

            try {
                if (lock.tryLock(5, TimeUnit.SECONDS)) {
                    try {
                        // DB 예약 가능 인원 수 차감
                        reservationService.reduceReservedCount(reservationInfo);
                        // DB 예약 정보 저장

                        // 채팅방 생성
//                        chatService.createChatRoom();

                        // 예약 확정 알림 전송

                        // Redis에서 임시 예약 정보 삭제
                        redisTemplate.delete("reservation:token:" + paymentResDto.getReservationToken());

                        return ResponseEntity.ok("예약이 완료되었습니다.");
                    } finally {
                        lock.unlock(); // 락 해제
                    }
                } else {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body("다른 예약 처리 중입니다. 잠시 후 다시 시도해주세요.");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("예약 처리 중 오류가 발생했습니다.");
            }
        } else {
            return ResponseEntity.badRequest().body("결제 확인에 실패했습니다.");
        }
    }
//
//            redisTemplate.delete("reservation:token:" + paymentResDto.getReservationToken());
//
//            chatService.createChatRoom(response.getReservation());
//
//            // TODO 예약 확정 알림 연결
//
//            return ResponseEntity.ok(response);
//        } else {
//            // TODO 결제 실패 시 처리
//            // TODO 임시 예약 시 인원수 복구
//            return ResponseEntity.badRequest().body("결제 확인에 실패했습니다.");
//        }
//}

    /**
     * 예약 취소
     * - 채팅방 비활성화
     * - 예약 상태 변경 - CANCELED
     */
    @PutMapping("/{reservationId}")
    public ResponseEntity<?> cancelReservation(@PathVariable Long reservationId) {
        try {
            boolean isCanceled = reservationService.cancelReservation(reservationId);

            if (isCanceled) {
                chatService.inactivateChatRoom(reservationId);
                // TODO 환불 규정에 따른 환불
                // TODO 예약 취소 알림
                return ResponseEntity.ok("예약이 성공적으로 취소되었습니다.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 예약을 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("예약 취소 중 오류가 발생했습니다.");
        }
    }

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
