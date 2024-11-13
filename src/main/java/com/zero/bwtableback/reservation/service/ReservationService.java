package com.zero.bwtableback.reservation.service;

import com.zero.bwtableback.common.exception.CustomException;
import com.zero.bwtableback.common.exception.ErrorCode;
import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.member.repository.MemberRepository;
import com.zero.bwtableback.reservation.dto.PaymentCompleteResDto;
import com.zero.bwtableback.reservation.dto.ReservationCreateReqDto;
import com.zero.bwtableback.reservation.dto.ReservationResDto;
import com.zero.bwtableback.reservation.dto.ReservationUpdateReqDto;
import com.zero.bwtableback.reservation.entity.NotificationType;
import com.zero.bwtableback.reservation.entity.Reservation;
import com.zero.bwtableback.reservation.entity.ReservationStatus;
import com.zero.bwtableback.reservation.repository.ReservationRepository;
import com.zero.bwtableback.restaurant.dto.RestaurantInfoDto;
import com.zero.bwtableback.restaurant.entity.Restaurant;
import com.zero.bwtableback.restaurant.repository.RestaurantRepository;
import com.zero.bwtableback.restaurant.service.RestaurantService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RestaurantRepository restaurantRepository;
    private final MemberRepository memberRepository;
    private final NotificationScheduleService notificationScheduleService;
    private final RestaurantService restaurantService;

    public ReservationResDto getReservationById(Long reservationId) {
        Reservation reservation = findReservationById(reservationId);
        return ReservationResDto.fromEntity(reservation);
    }

    // 고객이 생성한 예약 정보를 저장
    public ReservationResDto createReservation(ReservationCreateReqDto reservationCreateReqDto, Long restaurantId, Long memberId) {
        Restaurant restaurant = findRestaurantById(restaurantId);
        Member member = findMemberById(memberId);

        Reservation reservation = ReservationCreateReqDto.toEntity(reservationCreateReqDto, restaurant, member);
        Reservation savedReservation = reservationRepository.save(reservation);
        return ReservationResDto.fromEntity(savedReservation);
    }

    // CONFIRMED 상태 업데이트
    public PaymentCompleteResDto confirmReservation(Long reservationId, Long restaurantId) {
        Reservation reservation = findReservationById(reservationId);
        if (reservation.getReservationStatus() == ReservationStatus.CONFIRMED) {
            throw new CustomException(ErrorCode.INVALID_STATUS_CONFIRM);
        }
        reservation.setReservationStatus(ReservationStatus.CONFIRMED);

        // 예약 확정 알림 전송 및 스케줄링
        notificationScheduleService.scheduleImmediateNotification(reservation, NotificationType.CONFIRMATION);
        notificationScheduleService.schedule24HoursBeforeNotification(reservation);

        RestaurantInfoDto restaurantInfoDto = restaurantService.getRestaurantById(restaurantId);
        return PaymentCompleteResDto.fromEntities(restaurantInfoDto, reservation);
    }

    // CONFIRMED 제외한 나머지 상태 업데이트
    public ReservationResDto updateReservationStatus(Long reservationId, ReservationUpdateReqDto statusUpdateDto) {
        Reservation reservation = findReservationById(reservationId);
        ReservationStatus newStatus = statusUpdateDto.reservationStatus();

        return switch (newStatus) {
            case CUSTOMER_CANCELED -> handleCustomerCanceledStatus(reservation);
            case OWNER_CANCELED -> handleOwnerCanceledStatus(reservation);
            case NO_SHOW -> handleNoShowStatus(reservation);
            case VISITED -> handleVisitedStatus(reservation);
            default -> throw new CustomException(ErrorCode.INVALID_RESERVATION_STATUS);
        };
    }

    // CUSTOMER_CANCELED 상태 처리
    private ReservationResDto handleCustomerCanceledStatus(Reservation reservation) {
        if (reservation.getReservationStatus() != ReservationStatus.CONFIRMED) {
            throw new CustomException(ErrorCode.INVALID_STATUS_CUSTOMER_CANCEL);
        }

        LocalDate today = LocalDate.now();
        if (!canCancelReservation(reservation.getReservationDate(), today)) {
            throw new CustomException(ErrorCode.CUSTOMER_CANCEL_TOO_LATE);
        }

        reservation.setReservationStatus(ReservationStatus.CUSTOMER_CANCELED);
        notificationScheduleService.scheduleImmediateNotification(reservation, NotificationType.CANCELLATION);

        return ReservationResDto.fromEntity(reservation);
    }

    // OWNER_CANCELED 상태 처리
    private ReservationResDto handleOwnerCanceledStatus(Reservation reservation) {
        if (reservation.getReservationStatus() != ReservationStatus.CONFIRMED) {
            throw new CustomException(ErrorCode.INVALID_STATUS_OWNER_CANCEL);
        }
        reservation.setReservationStatus(ReservationStatus.OWNER_CANCELED);
        notificationScheduleService.scheduleImmediateNotification(reservation, NotificationType.CANCELLATION);

        return ReservationResDto.fromEntity(reservation);
    }

    // NO_SHOW 상태 처리
    private ReservationResDto handleNoShowStatus(Reservation reservation) {
        if (reservation.getReservationStatus() == ReservationStatus.CUSTOMER_CANCELED ||
                reservation.getReservationStatus() == ReservationStatus.OWNER_CANCELED) {
            throw new CustomException(ErrorCode.INVALID_STATUS_NO_SHOW);
        }
        reservation.setReservationStatus(ReservationStatus.NO_SHOW);

        return ReservationResDto.fromEntity(reservation);
    }

    // VISITED 상태 처리
    private ReservationResDto handleVisitedStatus(Reservation reservation) {
        if (reservation.getReservationStatus() != ReservationStatus.CONFIRMED) {
            throw new CustomException(ErrorCode.INVALID_STATUS_VISITED);
        }
        reservation.setReservationStatus(ReservationStatus.VISITED);

        return ReservationResDto.fromEntity(reservation);
    }

    public Reservation findReservationById(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));
    }

    private Restaurant findRestaurantById(Long restaurantId) {
        return restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESTAURANT_NOT_FOUND));
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private boolean canCancelReservation(LocalDate reservationDate, LocalDate currentDate) {
        return !reservationDate.minusDays(3).isBefore(currentDate);
    }

}
