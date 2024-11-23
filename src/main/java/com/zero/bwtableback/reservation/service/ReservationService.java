package com.zero.bwtableback.reservation.service;

import com.zero.bwtableback.chat.service.ChatService;
import com.zero.bwtableback.common.exception.CustomException;
import com.zero.bwtableback.common.exception.ErrorCode;
import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.member.entity.Role;
import com.zero.bwtableback.member.repository.MemberRepository;
import com.zero.bwtableback.payment.PaymentService;
import com.zero.bwtableback.reservation.dto.PaymentCompleteResDto;
import com.zero.bwtableback.reservation.dto.ReservationCompleteResDto;
import com.zero.bwtableback.reservation.dto.ReservationCreateReqDto;
import com.zero.bwtableback.reservation.dto.ReservationResDto;
import com.zero.bwtableback.reservation.entity.NotificationType;
import com.zero.bwtableback.reservation.entity.Reservation;
import com.zero.bwtableback.reservation.entity.ReservationStatus;
import com.zero.bwtableback.reservation.repository.ReservationRepository;
import com.zero.bwtableback.restaurant.dto.ReservationAvailabilityDto;
import com.zero.bwtableback.restaurant.dto.RestaurantInfoDto;
import com.zero.bwtableback.restaurant.dto.RestaurantResDto;
import com.zero.bwtableback.restaurant.entity.*;
import com.zero.bwtableback.restaurant.repository.ReservationSettingRepository;
import com.zero.bwtableback.restaurant.repository.RestaurantRepository;
import com.zero.bwtableback.restaurant.repository.TimeslotSettingRepository;
import com.zero.bwtableback.restaurant.repository.WeekdaySettingRepository;
import com.zero.bwtableback.restaurant.service.RestaurantService;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Slf4j
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
    private final PaymentService paymentService;
    private final ChatService chatService;

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisTemplate<String, Integer> integerRedisTemplate;

    public ReservationResDto getReservationById(Long reservationId) {
        Reservation reservation = findReservationById(reservationId);
        return ReservationResDto.fromEntity(reservation);
    }

    /**
     * 예약 가능 여부 확인
     */
    public ReservationAvailabilityDto checkReservationAvailability(ReservationCreateReqDto request) {
        try {
            LocalDate today = LocalDate.now();
            // 현재 날짜 이전 및 당일 예약 불가
            if (request.reservationDate().isBefore(today) || request.reservationDate().isEqual(today)) {
                return new ReservationAvailabilityDto(false, "현재 날짜 이전 및 당일 예약은 불가능합니다.");
            }

            restaurantRepository.findById(request.restaurantId())
                    .orElseThrow(() -> new CustomException(ErrorCode.RESTAURANT_NOT_FOUND));

            // 예약 기간 설정 확인
            ReservationSetting reservationSetting = findReservationSetting(request);
            // 요일 설정 확인
            WeekdaySetting weekdaySetting = findWeekdaySetting(reservationSetting, request.reservationDate());
            // 시간대 설정 확인
            TimeslotSetting timeslotSetting = findTimeslotSetting(weekdaySetting, request.reservationTime());

            String currentCountKey = String.format("reservation:currentCount:%d:%s:%s",
                    request.restaurantId(),
                    request.reservationDate(),
                    request.reservationTime());
            if (integerRedisTemplate.opsForValue().get(currentCountKey) == null) {
                integerRedisTemplate.opsForValue().set(currentCountKey, timeslotSetting.getMaxCapacity());
            }

            Integer availableReservedCount = integerRedisTemplate.opsForValue().get(currentCountKey); // 현재 예약 가능 수

            // 예약 가능 여부 확인
            if (request.numberOfPeople() > availableReservedCount) {
                return new ReservationAvailabilityDto(false, "예약 가능 인원을 초과했습니다.");
            }

            // 현재 가능 인원 갱신
            integerRedisTemplate.opsForValue().set(currentCountKey, availableReservedCount - request.numberOfPeople());

            return new ReservationAvailabilityDto(true, "예약 가능합니다.");
        } catch (CustomException e) {
            return new ReservationAvailabilityDto(false, e.getMessage());
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
    public void reduceReservedCount(ReservationCreateReqDto reservationInfo, String email) {
        ReservationSetting reservationSetting = findReservationSetting(reservationInfo);
        WeekdaySetting weekdaySetting = findWeekdaySetting(reservationSetting, reservationInfo.reservationDate());
        TimeslotSetting timeslotSetting = findTimeslotSetting(weekdaySetting, reservationInfo.reservationTime());

        int maxCapacity = timeslotSetting.getMaxCapacity();
        int reservedCount = getReservedCount(reservationInfo);

        if (reservedCount + reservationInfo.numberOfPeople() > maxCapacity) {
            throw new IllegalArgumentException("예약 가능 인원을 초과했습니다. (현재 예약 가능 인원: " + (maxCapacity - reservedCount) + ")");
        }

        // TODO 데이터베이스에서 예약 인원 차감

//        Restaurant restaurant = restaurantRepository.findById(reservationInfo.restaurantId())
//                .orElseThrow(() -> new CustomException(ErrorCode.RESTAURANT_NOT_FOUND));
//        Member member = memberRepository.findByEmail(email)
//                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

//        Reservation reservation = createReservation(reservationInfo, restaurant, member);
//        reservationRepository.save(reservation);

//        return new ReservationCompleteResDto(
//                RestaurantResDto.fromEntity(restaurant),
//                ReservationResDto.fromEntity(reservation)
//        );
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

    // 해당 식당의 날짜, 시간대에 예약된 인원수
    private int getReservedCount(ReservationCreateReqDto reservationInfo) {
        return reservationRepository.countReservedPeopleByRestaurantAndDateTime(
                reservationInfo.restaurantId(),
                reservationInfo.reservationDate(),
                reservationInfo.reservationTime()
        );
    }

    /**
     * 결제 성공 시 예약 정보 저장
     */
    public ReservationCompleteResDto saveReservation(ReservationCreateReqDto reservationInfo, Long reservationId) {
        Member member = memberRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Restaurant restaurant = findRestaurantById(reservationInfo.restaurantId());

        Reservation reservation = createReservation(reservationInfo, restaurant, member);
        reservationRepository.save(reservation);

        return new ReservationCompleteResDto(
                RestaurantResDto.fromEntity(restaurant),
                ReservationResDto.fromEntity(reservation)
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

    // 예약 확정 시 알림 전송
    public void emitNotification(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));

        try {
            notificationScheduleService.scheduleImmediateNotification(reservation, NotificationType.CONFIRMATION);
            notificationScheduleService.schedule24HoursBeforeNotification(reservation);
        } catch (Exception e) {
            log.error("Error emitting notification for reservation: {}", reservationId, e);
            throw new CustomException(ErrorCode.NOTIFICATION_SEND_FAILED);
        }
    }

    /**
     * 결제 실패 시 카운트 복구 (redis)
     */
    public void restoreReservedCount(ReservationCreateReqDto reservationInfo) {
        String availableCountKey = String.format("reservation:currentCount:%d:%s:%s",
                reservationInfo.restaurantId(),
                reservationInfo.reservationDate(),
                reservationInfo.reservationTime());

        Integer CurrentCountKey = integerRedisTemplate.opsForValue().get(availableCountKey);
        redisTemplate.opsForValue().set(availableCountKey, CurrentCountKey + reservationInfo.numberOfPeople());
    }

    // CONFIRMED 상태 업데이트
    public PaymentCompleteResDto confirmReservation(Long reservationId, Long memberId) {
        Reservation reservation = findReservationById(reservationId);
        Restaurant restaurant = findRestaurantById(reservation.getRestaurant().getId());
        findMemberById(memberId); // 사장님 객체

        // 사장님 가게 확인
        if (restaurant.getMember().getId() != memberId) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        // 예약 확정 확인
        if (reservation.getReservationStatus() == ReservationStatus.CONFIRMED) {
            throw new CustomException(ErrorCode.INVALID_STATUS_CONFIRM);
        }

        reservation.setReservationStatus(ReservationStatus.CONFIRMED);

        // 예약 확정 알림 전송 및 스케줄링
        notificationScheduleService.scheduleImmediateNotification(reservation, NotificationType.CONFIRMATION);
        notificationScheduleService.schedule24HoursBeforeNotification(reservation);

        RestaurantInfoDto restaurantInfoDto = restaurantService.getRestaurantById(restaurant.getId());
        return PaymentCompleteResDto.fromEntities(restaurantInfoDto, reservation);
    }

    /**
     * 손님/가게의 취소 요청
     */
    public String cancelReservation(Long reservationId, Long memberId) throws IOException {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 손님 역할일 경우 본인 예약 확인
        if (member.getRole() == Role.GUEST) {
            if (!reservation.getMember().getId().equals(memberId)) {
                throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS); // 본인 예약이 아닐 경우 예외 발생
            }
            handleCustomerCancel(reservation);
        } else { // 사장 역할일 경우 본인 가게의 예약 확인
            if (!reservation.getRestaurant().getId().equals(member.getRestaurant().getId())) {
                throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS); // 본인 가게의 예약이 아닐 경우 예외 발생
            }
            handleOwnerCancel(reservation);
        }

        // 채팅방 비활성화
        chatService.inactivateChatRoom(reservationId);

        return "예약이 성공적으로 취소되었습니다.";
    }

    // CONFIRMED, NOSHOW를 제외한 나머지 상태 업데이트
//    public ReservationResDto updateReservationStatus
//    (ReservationUpdateReqDto statusUpdateDto, Long reservationId, Long memberId) throws IOException {
//        Reservation reservation = findReservationById(reservationId);
//        Member member = findMemberById(memberId);
//
//        // FIXME 예약의 member는 손님이고, 사장님이 방문 처리 사용 시 문제
//        if (!member.getId().equals(reservation.getMember().getId())) {
//            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
//        }
//
//        if (statusUpdateDto.reservationStatus() == null) {
//            throw new CustomException(ErrorCode.INVALID_RESERVATION_STATUS);
//        }
//
//        ReservationStatus newStatus = statusUpdateDto.reservationStatus();
//
//        return switch (newStatus) {
//            case CUSTOMER_CANCELED -> handleCustomerCanceledStatus(reservation);
//            case OWNER_CANCELED -> handleOwnerCanceledStatus(reservation);
//
////            case NO_SHOW -> handleNoShowStatus(reservation);
////            case VISITED -> handleVisitedStatus(reservation);
////          FIXME  default -> throw new CustomException(ErrorCode.INVALID_RESERVATION_STATUS);
//            default -> throw new RuntimeException("INVALID_RESERVATION_STATUS");
////            case NO_SHOW -> handleNoShowStatus(reservation);
////            case VISITED -> handleVisitedStatus(reservation);
////            default ->
////                    throw new CustomException(ErrorCode.INVALID_RESERVATION_STATUS);
//        };
//    }

    // 회원의 예약 취소
    private ReservationResDto handleCustomerCancel(Reservation reservation) {
        if (reservation.getReservationStatus() != ReservationStatus.CONFIRMED) {
            throw new CustomException(ErrorCode.INVALID_STATUS_CUSTOMER_CANCEL);
        }

        // FIXME 예약 취소 테스트를 위한 현재 날짜 임시 변경
//        LocalDate today = LocalDate.now();
        LocalDate today = LocalDate.of(2024, 11, 17);
        if (!canCancelReservation(reservation.getReservationDate(), today)) {
            throw new CustomException(ErrorCode.CUSTOMER_CANCEL_TOO_LATE);
        }

        // TODO 예약 3일 전 환불 가능
        paymentService.refundReservationDeposit(reservation.getId());

        reservation.setReservationStatus(ReservationStatus.CUSTOMER_CANCELED);
        notificationScheduleService.scheduleImmediateNotification(reservation, NotificationType.CANCELLATION);

        return ReservationResDto.fromEntity(reservation);
    }

    // 사장님의 예약 취소
    private ReservationResDto handleOwnerCancel(Reservation reservation) throws IOException {
        if (reservation.getReservationStatus() != ReservationStatus.CONFIRMED) {
            throw new CustomException(ErrorCode.INVALID_STATUS_OWNER_CANCEL);
        }
        reservation.setReservationStatus(ReservationStatus.OWNER_CANCELED);

        // TODO 예약 3일 전 환불 가능
        paymentService.refundReservationDeposit(reservation.getId());

        notificationScheduleService.scheduleImmediateNotification(reservation, NotificationType.CANCELLATION);

        return ReservationResDto.fromEntity(reservation);
    }

    /**
     * VISITED 상태 처리
     * - 사장님이 판단 후에 선택 가능
     */
    public ReservationResDto handleVisitedStatus(Long reservationId, Long memberId) {
        Reservation reservation = findReservationById(reservationId);
        Member member = findMemberById(memberId); // 사장님 정보

        // 예약된 가게가 사장님 소유 여부 확인
        if (member.getRestaurant() == null ||
                reservation.getRestaurant().getId() != member.getRestaurant().getId()) {
            throw new CustomException(ErrorCode.RESTAURANT_OWNERSHIP_MISMATCH);
        }

        if (reservation.getReservationStatus() != ReservationStatus.CONFIRMED ||
                reservation.getReservationStatus() == ReservationStatus.CUSTOMER_CANCELED ||
                reservation.getReservationStatus() == ReservationStatus.OWNER_CANCELED) {
            throw new CustomException(ErrorCode.INVALID_STATUS_VISITED);
        }

        // 예약금 전액 환불
        paymentService.refundReservationDeposit(reservation.getId());

        // 채팅방 비활성화
        chatService.inactivateChatRoom(reservation.getId());

        reservation.setReservationStatus(ReservationStatus.VISITED);
        reservationRepository.save(reservation);

        return ReservationResDto.fromEntity(reservation);
    }

    /**
     * NO SHOW 상태 처리
     * - 사장님이 판단 후에 선택 가능
     */
    public ReservationResDto handleNoShowStatus(Long reservationId, Long memberId) {
        Reservation reservation = findReservationById(reservationId);
        Member member = findMemberById(memberId);

        // 예약된 가게가 사장님 소유 여부 확인
        if (member.getRestaurant() == null ||
                reservation.getRestaurant().getId() != member.getRestaurant().getId()) {
            throw new CustomException(ErrorCode.RESTAURANT_OWNERSHIP_MISMATCH);
        }

        if (reservation.getReservationStatus() != ReservationStatus.CONFIRMED ||
                reservation.getReservationStatus() == ReservationStatus.CUSTOMER_CANCELED ||
                reservation.getReservationStatus() == ReservationStatus.OWNER_CANCELED) {
            throw new CustomException(ErrorCode.INVALID_STATUS_NO_SHOW);
        }

        // 채팅방 비활성화
        chatService.inactivateChatRoom(reservation.getId());

        reservation.setReservationStatus(ReservationStatus.NO_SHOW);
        reservationRepository.save(reservation);

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

    /**
     * 예약 대시보드
     * 특정 식당의 예약 내역 조회
     */
    public List<Reservation> getReservationByRestaurant(Long restaurantId, LocalDate reservationDate) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found"));

        if (reservationDate != null) {
            return reservationRepository.findByRestaurantIdAndReservationDate(restaurantId, reservationDate);
        }

        return reservationRepository.findByRestaurantId(restaurantId);
    }
}
