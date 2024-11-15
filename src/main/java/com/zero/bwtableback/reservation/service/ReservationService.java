package com.zero.bwtableback.reservation.service;

import com.zero.bwtableback.common.exception.CustomException;
import com.zero.bwtableback.common.exception.ErrorCode;
import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.member.repository.MemberRepository;
import com.zero.bwtableback.reservation.dto.*;
import com.zero.bwtableback.reservation.entity.NotificationType;
import com.zero.bwtableback.reservation.entity.Reservation;
import com.zero.bwtableback.reservation.entity.ReservationStatus;
import com.zero.bwtableback.reservation.repository.ReservationRepository;
import com.zero.bwtableback.restaurant.dto.RestaurantInfoDto;
import com.zero.bwtableback.restaurant.dto.RestaurantResDto;
import com.zero.bwtableback.restaurant.entity.Restaurant;
import com.zero.bwtableback.restaurant.repository.RestaurantRepository;
import com.zero.bwtableback.restaurant.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RestaurantRepository restaurantRepository;
    private final MemberRepository memberRepository;
    private final NotificationScheduleService notificationScheduleService;
    private final RestaurantService restaurantService;

    private final RedisTemplate<String, Object> redisTemplate;

    public ReservationResDto getReservationById(Long reservationId) {
        Reservation reservation = findReservationById(reservationId);
        return ReservationResDto.fromEntity(reservation);
    }

    // FIXME 사용 안함: 고객이 생성한 예약 정보를 저장
    public ReservationResDto createReservation(ReservationCreateReqDto reservationCreateReqDto, Long memberId) {
        Restaurant restaurant = findRestaurantById(reservationCreateReqDto.restaurantId());
        Member member = findMemberById(memberId);

        Reservation reservation = ReservationCreateReqDto.toEntity(reservationCreateReqDto, restaurant, member);
        Reservation savedReservation = reservationRepository.save(reservation);
        return ReservationResDto.fromEntity(savedReservation);
    }

    public boolean checkReservationAvailability(ReservationCreateReqDto request) {

        /**
         * TODO 현재 인원수 보다 작거나 같은지 확인, 가능성 확인
         *
         * - 현재 예약 가능 인원수와 요청 예약 인원수 비교
         * - DB에 예약 가능 설정에서 차감된 수 저장
         */
        Restaurant restaurant = restaurantRepository.findById(request.restaurantId())
                .orElseThrow(() -> new CustomException(ErrorCode.RESTAURANT_NOT_FOUND));

        // 최대 수용 인원을 초과
//        if (currentReservedCount + request.getNumberOfPeople() > MAX_CAPACITY) {
//            return false;
//        }

        return true;
    }

    public void reduceReservedCount(ReservationCreateReqDto reservationInfo) {
//        TimeslotSetting timeslotSetting = reservationRepository.findTimeslotSetting(
//                reservationInfo.getRestaurantId(),
//                reservationInfo.getReservationDate(),
//                reservationInfo.getReservationTime()
//        );
//
//        if (timeslotSetting != null) {
//            // 현재 최대 수용 인원수에서 예약할 인원수를 차감
//            int currentCapacity = timeslotSetting.getMaxCapacity();
//            int reservedCount = timeslotSetting.getReservedCount(); // 현재 예약된 인원 수
//
//            if (reservedCount + reservationInfo.getNumberOfPeople() <= currentCapacity) {
//                // 예약 가능하다면 예약된 인원 수를 증가시킴
//                timeslotSetting.setReservedCount(reservedCount + reservationInfo.getNumberOfPeople());
//                reservationRepository.save(timeslotSetting); // 변경 사항 저장
//            } else {
//                throw new IllegalArgumentException("예약 가능 인원을 초과했습니다.");
//            }
//        } else {
//            throw new EntityNotFoundException("해당 시간대의 설정을 찾을 수 없습니다.");
//        }

    }

    /**
     * 결제 성공 시 예약 정보 저장
     */
    public ReservationCompleteResDto saveReservation(PaymentResDto paymentResDto, String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // Redis에서 임시 예약 정보 조회 TODO ReservationCreateDto ReservationInfoDto로 통합
        Object redisValue = redisTemplate.opsForValue().get("reservation:token:" + paymentResDto.getReservationToken());
        if (redisValue == null) {
            throw new RuntimeException("예약 토큰이 존재하지 않습니다.");
        }
        if (redisValue instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) redisValue;

            Long restaurantId = Long.valueOf(map.get("restaurantId").toString());
            Restaurant restaurant = restaurantRepository.findById(restaurantId)
                    .orElseThrow(() -> new RuntimeException("Restaurant not found"));

            List<Integer> dateList = (List<Integer>) map.get("reservationDate");
            LocalDate reservationDate = LocalDate.of(dateList.get(0), dateList.get(1), dateList.get(2));

            List<Integer> timeList = (List<Integer>) map.get("reservationTime");
            LocalTime reservationTime = LocalTime.of(timeList.get(0), timeList.get(1));

            int numberOfPeople = (Integer) map.get("numberOfPeople");
            String specialRequest = (String) map.get("specialRequest");

            Reservation reservation = Reservation.builder()
                    .restaurant(restaurant)
                    .member(member)
                    .reservationDate(reservationDate)
                    .reservationTime(reservationTime)
                    .numberOfPeople(numberOfPeople)
                    .specialRequest(specialRequest)
                    .reservationStatus(ReservationStatus.CONFIRMED)
                    .build();

            reservationRepository.save(reservation);

            return new ReservationCompleteResDto(
                    RestaurantResDto.fromEntity(restaurant),
                    ReservationResDto.fromEntity(reservation)
            );
        } else {
            throw new RuntimeException("레디스에 저장된 데이터 형식과 다릅니다.");
        }
    }

    // CONFIRMED 상태 업데이트
    public PaymentCompleteResDto confirmReservation(Long reservationId, Long restaurantId, Long memberId) {
        Reservation reservation = findReservationById(reservationId);
        Member member = findMemberById(memberId);

        if (!member.getId().equals(reservation.getMember().getId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

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
    public ReservationResDto updateReservationStatus(ReservationUpdateReqDto statusUpdateDto,
                                                     Long reservationId,
                                                     Long memberId) {
        Reservation reservation = findReservationById(reservationId);
        Member member = findMemberById(memberId);

        if (!member.getId().equals(reservation.getMember().getId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        if (statusUpdateDto.reservationStatus() == null) {
//          FIXME  throw new CustomException(ErrorCode.INVALID_RESERVATION_STATUS);
        }

        ReservationStatus newStatus = statusUpdateDto.reservationStatus();

        return switch (newStatus) {
            case CUSTOMER_CANCELED -> handleCustomerCanceledStatus(reservation);
            case OWNER_CANCELED -> handleOwnerCanceledStatus(reservation);
            case NO_SHOW -> handleNoShowStatus(reservation);
            case VISITED -> handleVisitedStatus(reservation);
//          FIXME  default -> throw new CustomException(ErrorCode.INVALID_RESERVATION_STATUS);
            default -> throw new RuntimeException("INVALID_RESERVATION_STATUS");
        };
    }

    /**
     * 손님의 취소 요청
     */
    public boolean cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));
        if (reservation == null) {
            return false;
        }
        reservation.setReservationStatus(ReservationStatus.CUSTOMER_CANCELED);
        reservationRepository.save(reservation);

        return true;
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
