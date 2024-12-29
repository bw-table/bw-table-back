package com.zero.bwtableback.reservation.controller;

import com.siot.IamportRestClient.response.Payment;
import com.zero.bwtableback.chat.service.ChatService;
import com.zero.bwtableback.common.exception.CustomException;
import com.zero.bwtableback.common.exception.ErrorCode;
import com.zero.bwtableback.member.dto.MemberDto;
import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.member.service.MemberService;
import com.zero.bwtableback.payment.PaymentService;
import com.zero.bwtableback.reservation.dto.*;
import com.zero.bwtableback.reservation.entity.Reservation;
import com.zero.bwtableback.reservation.service.ReservationService;
import com.zero.bwtableback.restaurant.dto.ReservationAvailabilityDto;
import com.zero.bwtableback.security.MemberDetails;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final MemberService memberService;
    private final ChatService chatService;
    private final PaymentService paymentService;

    private final RedissonClient redissonClient;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 예약 아이디로 예약 상세 조회
     */
    @GetMapping("/{reservationId}")
    public ReservationResDto getReservationById(@PathVariable Long reservationId) {
        return reservationService.getReservationById(reservationId);
    }

    /**
     * 예약 생성 요청을 처리
     * 1. 예약 가능 여부를 확인
     * 2. 예약이 가능한 경우, 임시 예약 정보를 Redis에 저장
     * 3. 현재 가능 인원 갱신
     * 4. 생성된 예약 토큰을 클라이언트에 반환합니다.
     */
    @PostMapping()
    public ResponseEntity<?> requestReservation(@RequestBody ReservationCreateReqDto request,
                                                @AuthenticationPrincipal MemberDetails memberDetails) {
        ReservationAvailabilityDto availability = reservationService.checkReservationAvailability(request);
        if (availability.isAvailable()) {
            ReservationCreateResDto reservationCreateResDto = reservationService.createReservation(memberDetails.getMemberId(), request.restaurantId());
            redisTemplate.opsForValue().set("reservation:token:" + reservationCreateResDto.getReservationToken(), request, 5, TimeUnit.MINUTES);
            return ResponseEntity.ok(reservationCreateResDto);
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
    public ResponseEntity<?> completeReservation(@RequestBody PaymentReqDto paymentReqDto,
                                                 @AuthenticationPrincipal MemberDetails memberDetails) {
        ReservationCreateReqDto reservationInfo = reservationService.getReservationInfo(paymentReqDto.getReservationToken());
        Payment payment = paymentService.verifyPayment(paymentReqDto);
        if ("paid".equals(payment.getStatus())) {
            String lockKey = "lock:reservation:" + reservationInfo.restaurantId() + ":" + reservationInfo.reservationDate() + ":" + reservationInfo.reservationTime();
            RLock lock = redissonClient.getLock(lockKey);

            ReservationCompleteResDto response = null;
            try {
                if (lock.tryLock(5, TimeUnit.SECONDS)) {
                    try {
                        // DB 예약 가능 인원 수 차감
                        reservationService.reduceReservedCount(reservationInfo, memberDetails.getUsername());
                        // 예약 정보 저장
                        response = reservationService.saveReservation(reservationInfo, memberDetails.getMemberId());
                        // Redis에서 임시 예약 정보 삭제
                        redisTemplate.delete("reservation:token:" + paymentReqDto.getReservationToken());
                    } finally {
                        lock.unlock(); // 락 해제
                    }
                    if (response != null) {
                        // 채팅방 생성
                        Long chatRoomId =  chatService.createChatRoom(response.getReservation());
                        response.setChatRoomId(chatRoomId);

                        // 예약 확정 알림 전송 및 스케줄링
                        reservationService.emitNotification(response.getReservation().reservationId());

                        // 결제 정보 저장
                        paymentService.verifiedPaymentSave(payment, response);
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
            // 결제 실패 시 예약 가능 인원수 복구
            reservationService.restoreReservedCount(reservationInfo);
            return ResponseEntity.badRequest().body("결제 확인에 실패했습니다.");
        }
    }

    /**
     * 예약 취소
     * - 채팅방 비활성화
     * - 예약 상태 변경(GUEST_CANCELED/OWNER_CANCELED)
     * - 예약 취소 알림
     * - 환불 규정에 따른 환불
     */
    @PutMapping("/{reservationId}/cancel")
    @Operation(summary = "예약 취소", description = "주어진 예약 ID로 예약을 취소합니다.")
    public ResponseEntity<?> cancelReservation(@PathVariable Long reservationId,
                                               @AuthenticationPrincipal MemberDetails memberDetails) {
        try {
            return ResponseEntity.ok(reservationService.cancelReservation(reservationId, memberDetails.getMemberId()));
        } catch (CustomException e) {
            return ResponseEntity.status(e.getErrorCode().getHttpStatus())
                    .body(e.getMessage());
        } catch (IOException e) {
            log.error("예약 취소 중 예상치 못한 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body((ErrorCode.INTERNAL_SERVER_ERROR));
        }
    }

    @PutMapping("/{reservationId}/visit")
    @Operation(summary = "사장님의 방문 처리", description = "주어진 예약 ID로 방문 처리를 합니다.")
    public ResponseEntity<?> handleVisited(
            @PathVariable Long reservationId,
            @AuthenticationPrincipal MemberDetails memberDetails) {
        return ResponseEntity.ok(reservationService.handleVisitedStatus(reservationId, memberDetails.getMemberId()));
    }

    @PutMapping("/{reservationId}/noshow")
    @Operation(summary = "사장님의 노쇼 처리", description = "주어진 예약 ID로 노쇼 처리를 합니다.")
    public ResponseEntity<?> handleNoShow(
            @PathVariable Long reservationId,
            @AuthenticationPrincipal MemberDetails memberDetails) {
        return ResponseEntity.ok(reservationService.handleNoShowStatus(reservationId, memberDetails.getMemberId()));
    }

    /**
     * 예약 대시보드
     * 특정 식당의 예약 내역 조회
     */
    @GetMapping("/restaurants/{restaurantId}")
    public ResponseEntity<List<ReservationResDto>> getReservationsByRestaurantId(@PathVariable Long restaurantId,
                                                                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                                                 Pageable pageable) {
        List<ReservationResDto> reservations = reservationService.getReservationByRestaurant(restaurantId, date, pageable);
        return ResponseEntity.ok(reservations);
    }
}
