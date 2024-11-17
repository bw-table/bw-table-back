package com.zero.bwtableback.reservation.controller;

import com.zero.bwtableback.chat.service.ChatService;
import com.zero.bwtableback.payment.PaymentService;
import com.zero.bwtableback.reservation.dto.*;
import com.zero.bwtableback.reservation.service.ReservationService;
import com.zero.bwtableback.restaurant.dto.ReservationAvailabilityDto;
import com.zero.bwtableback.security.MemberDetails;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
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
     * 1. 예약 가능 여부를 확인
     * 2. 예약이 가능한 경우, 임시 예약 정보를 Redis에 저장
     * 3. 반환된 예약 토큰을 클라이언트에 전달하여 결제 시 사용
     */
    @PostMapping()
    public ResponseEntity<?> requestReservation(@RequestBody ReservationCreateReqDto request,
                                                @AuthenticationPrincipal MemberDetails memberDetails) {
        ReservationAvailabilityDto availability = reservationService.checkReservationAvailability(request);
        if (availability.isAvailable()) {
            String reservationToken = UUID.randomUUID().toString();
            redisTemplate.opsForValue().set("reservation:token:" + reservationToken, request, 5, TimeUnit.MINUTES);
            return ResponseEntity.ok(reservationToken);
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(availability.getMessage());
        }
    }

    /**
     * 예약 확정 및 결제 완료
     *
     * 1. 결제 정보와 함께 요청이 들어오면, 해당 예약 정보를 조회
     * 2. 결제가 성공적으로 완료되면, 분산 락을 사용하여 동시성 제어
     * 3. 현재 예약된 인원 수를 확인하고, 최대 결제 인원을 초과하지 않는 경우에만 예약을 확정하고 DB에 저장
     * 4. 채팅방을 생성하고 Redis에서 임시 예약 정보를 삭제
     *
     * @return 결제 완료 페이지에 보여질 정보 반환
     */
    @PostMapping("/complete")
    public ResponseEntity<?> completeReservation(@RequestBody PaymentResDto paymentResDto,
                                                 @AuthenticationPrincipal MemberDetails memberDetails) {
        ReservationCreateReqDto reservationInfo = reservationService.getReservationInfo(paymentResDto.getReservationToken());

        if (paymentService.verifyPaymentAndSave(paymentResDto)) {
            String lockKey = "lock:reservation:" + reservationInfo.restaurantId() + ":" + reservationInfo.reservationDate() + ":" + reservationInfo.reservationTime();
            RLock lock = redissonClient.getLock(lockKey);

            ReservationCompleteResDto response = null;
            try {
                if (lock.tryLock(5, TimeUnit.SECONDS)) {
                    try {
                        // DB 예약 가능 인원 수 차감 및 예약 정보 저장
                        response = reservationService.reduceReservedCount(reservationInfo, memberDetails.getUsername());

                        // Redis에서 임시 예약 정보 삭제
                        redisTemplate.delete("reservation:token:" + paymentResDto.getReservationToken());
                    } finally {
                        lock.unlock(); // 락 해제
                    }
                    if (response != null) {
                        // 채팅방 생성
                        chatService.createChatRoom(response.getReservation());

                        // TODO 예약 확정 알림 전송
                    }
                    return ResponseEntity.ok(response);
                } else {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body("다른 예약 처리 중입니다. 잠시 후 다시 시도해주세요.");
                }
            } catch (InterruptedException e) {
                reservationService.restoreReservedCount(reservationInfo);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("예약 처리 중 오류가 발생했습니다.");
            }
        } else {
            // TODO 결제 실패 시 처리
            // TODO 예약 실패 알림 전송
            // 결제 실패 시 예약 가능 인원수 복구
            reservationService.restoreReservedCount(reservationInfo);
            return ResponseEntity.badRequest().body("결제 확인에 실패했습니다.");
        }
    }

    /**
     * 예약 취소
     * - 채팅방 비활성화
     * - 예약 상태 변경(GUEST_CANCELED/OWNER_CANCELED)
     */
    @PutMapping("/{reservationId}/cancel")
    public ResponseEntity<?> cancelReservation(@PathVariable Long reservationId,
                                               @AuthenticationPrincipal MemberDetails memberDetails) {
        try {
            return ResponseEntity.ok(reservationService.cancelReservation(reservationId, memberDetails.getMemberId()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("예약 취소 중 오류가 발생했습니다.");
        }
    }

    /**
     * 사장님의 방문 처리
     */
    @PutMapping("/{reservationId}/visited")
    public PaymentCompleteResDto handleVisited(
            @PathVariable Long reservationId,
            @RequestParam Long restaurantId,
            @AuthenticationPrincipal MemberDetails memberDetails) {
        return reservationService.confirmReservation(reservationId, restaurantId, memberDetails.getMemberId());
    }

    /**
     * 사장님의 노쇼 처리
     */
    @PutMapping("/{reservationId}/noshow")
    public PaymentCompleteResDto handleNoShow(
            @PathVariable Long reservationId,
            @RequestParam Long restaurantId,
            @AuthenticationPrincipal MemberDetails memberDetails) {
        return reservationService.confirmReservation(reservationId, restaurantId, memberDetails.getMemberId());
    }

    // FIXME 사용 여부 확인
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
            @AuthenticationPrincipal MemberDetails memberDetails) throws IOException {
        return reservationService.updateReservationStatus(statusUpdateDto, reservationId, memberDetails.getMemberId());
    }
}
