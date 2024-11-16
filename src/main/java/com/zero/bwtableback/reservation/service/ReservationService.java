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
import com.zero.bwtableback.restaurant.dto.ReservationAvailabilityResult;
import com.zero.bwtableback.restaurant.dto.RestaurantInfoDto;
import com.zero.bwtableback.restaurant.dto.RestaurantResDto;
import com.zero.bwtableback.restaurant.entity.*;
import com.zero.bwtableback.restaurant.repository.ReservationSettingRepository;
import com.zero.bwtableback.restaurant.repository.RestaurantRepository;
import com.zero.bwtableback.restaurant.repository.TimeslotSettingRepository;
import com.zero.bwtableback.restaurant.repository.WeekdaySettingRepository;
import com.zero.bwtableback.restaurant.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final MemberRepository memberRepository;
    private final ReservationRepository reservationRepository;
    private final RestaurantRepository restaurantRepository;
    private final ReservationSettingRepository reservationSettingRepository;
    private final WeekdaySettingRepository weekdaySettingRepository;
    private final TimeslotSettingRepository timeslotSettingRepository;


    private final NotificationScheduleService notificationScheduleService;
    private final RestaurantService restaurantService;

    private final RedisTemplate<String, Object> redisTemplate;

    public ReservationResDto getReservationById(Long reservationId) {
        Reservation reservation = findReservationById(reservationId);
        return ReservationResDto.fromEntity(reservation);
    }

    public ReservationAvailabilityResult checkReservationAvailability(ReservationCreateReqDto request) {
        try {
            LocalDate today = LocalDate.now();
            // 현재 날짜 이전 및 당일 예약 불가
            if (request.reservationDate().isBefore(today) || request.reservationDate().isEqual(today)) {
                return new ReservationAvailabilityResult(false, "현재 날짜 이전 및 당일 예약은 불가능합니다.");
            }

            restaurantRepository.findById(request.restaurantId())
                    .orElseThrow(() -> new CustomException(ErrorCode.RESTAURANT_NOT_FOUND));

            // 예약 기간 설정 확인
            ReservationSetting reservationSetting = findReservationSetting(request);
            // 요일 설정 확인
            WeekdaySetting weekdaySetting = findWeekdaySetting(reservationSetting, request.reservationDate());
            // 시간대 설정 확인
            TimeslotSetting timeslotSetting = findTimeslotSetting(weekdaySetting, request.reservationTime());

            // 현재 예약된 인원 수 조회
            int currentReservedCount = getReservedCount(request);

            // 예약 가능 여부 확인
            int maxCapacity = timeslotSetting.getMaxCapacity();

            if (currentReservedCount + request.numberOfPeople() > maxCapacity) {
                return new ReservationAvailabilityResult(false, "예약 가능 인원을 초과했습니다.");
            }

            return new ReservationAvailabilityResult(true, "예약 가능합니다.");
        } catch (CustomException e) {
            return new ReservationAvailabilityResult(false, e.getMessage());
        }
    }

    /**
     * 레디스에 임시 저장 예약 정보 반환
     */
    public ReservationCreateReqDto getReservationInfo(String reservationToken
    ) {
        String reservationKey = "reservation:token:" + reservationToken;
        Object redisValue = redisTemplate.opsForValue().get(reservationKey);

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

            return new ReservationCreateReqDto(
                    restaurantId,
                    reservationDate,
                    reservationTime,
                    numberOfPeople,
                    specialRequest
            );
        } else {
            throw new RuntimeException("예약 정보 형식이 올바르지 않습니다.");
        }
    }

    @Transactional
    public ReservationCompleteResDto reduceReservedCount(ReservationCreateReqDto reservationInfo, String email) {
        ReservationSetting reservationSetting = findReservationSetting(reservationInfo);
        WeekdaySetting weekdaySetting = findWeekdaySetting(reservationSetting, reservationInfo.reservationDate());
        TimeslotSetting timeslotSetting = findTimeslotSetting(weekdaySetting, reservationInfo.reservationTime());

        int maxCapacity = timeslotSetting.getMaxCapacity();
        int reservedCount = getReservedCount(reservationInfo);

        if (reservedCount + reservationInfo.numberOfPeople() > maxCapacity) {
            throw new IllegalArgumentException("예약 가능 인원을 초과했습니다. (현재 예약 가능 인원: " + (maxCapacity - reservedCount) + ")");
        }

        Restaurant restaurant = restaurantRepository.findById(reservationInfo.restaurantId())
                .orElseThrow(() -> new CustomException(ErrorCode.RESTAURANT_NOT_FOUND));
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Reservation reservation = createReservation(reservationInfo, restaurant, member);
        reservationRepository.save(reservation);

        return new ReservationCompleteResDto(
                RestaurantResDto.fromEntity(restaurant),
                ReservationResDto.fromEntity(reservation)
        );
    }

    private ReservationSetting findReservationSetting(ReservationCreateReqDto reservationInfo) {
        return reservationSettingRepository.findByRestaurantIdAndDateRange(reservationInfo.restaurantId(), reservationInfo.reservationDate())
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_SETTING_NOT_FOUND));
    }

    private WeekdaySetting findWeekdaySetting(ReservationSetting reservationSetting, LocalDate date) {
        DayOfWeek dayOfWeek = DayOfWeek.valueOf(date.getDayOfWeek().name());
        return weekdaySettingRepository.findByReservationSettingIdAndDayOfWeek(reservationSetting.getId(), dayOfWeek)
                .orElseThrow(() -> new CustomException(ErrorCode.WEEKDAY_SETTING_NOT_FOUND));
    }

    private TimeslotSetting findTimeslotSetting(WeekdaySetting weekdaySetting, LocalTime time) {
        return timeslotSettingRepository.findByWeekdaySettingAndTimeslot(weekdaySetting, time)
                .orElseThrow(() -> new CustomException(ErrorCode.TIMESLOT_SETTING_NOT_FOUND));
    }

    private int getReservedCount(ReservationCreateReqDto reservationInfo) {
        return reservationRepository.countReservedPeopleByRestaurantAndDateTime(
                reservationInfo.restaurantId(),
                reservationInfo.reservationDate(),
                reservationInfo.reservationTime()
        );
    }

    private Reservation createReservation(ReservationCreateReqDto reservationInfo, Restaurant restaurant, Member member) {
        return Reservation.builder()
                .restaurant(restaurant)
                .member(member)
                .reservationDate(reservationInfo.reservationDate())
                .reservationTime(reservationInfo.reservationTime())
                .numberOfPeople(reservationInfo.numberOfPeople())
                .specialRequest(reservationInfo.specialRequest())
                .reservationStatus(ReservationStatus.CONFIRMED)
                .build();
    }

    /**
     * 결제 성공 시 예약 정보 저장
     */
    public ReservationCompleteResDto saveReservation(PaymentResDto
                                                             paymentResDto, String email) {
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

    /**
     * 예약 복구
     */
//    public void restoreSeats(ReservationCreateReqDto request) {
//        Reservation reservation = reservationRepository.findById(request.getReservationId())
//                .orElseThrow(() -> new IllegalArgumentException("해당 예약을 찾을 수 없습니다."));
//
//        reservation.setAvailableSeats(reservation.getAvailableSeats() + request.getNumberOfPeople());
//        reservationRepository.save(reservation);
//    }

    // CONFIRMED 상태 업데이트
    public PaymentCompleteResDto confirmReservation(Long
                                                            reservationId, Long restaurantId, Long memberId) {
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
    public ReservationResDto updateReservationStatus
    (ReservationUpdateReqDto statusUpdateDto,
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
    private ReservationResDto handleCustomerCanceledStatus(Reservation
                                                                   reservation) {
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
    private ReservationResDto handleOwnerCanceledStatus(Reservation
                                                                reservation) {
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

    private boolean canCancelReservation(LocalDate
                                                 reservationDate, LocalDate currentDate) {
        return !reservationDate.minusDays(3).isBefore(currentDate);
    }
}
