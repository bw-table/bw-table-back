package com.zero.bwtableback.restaurant.service;

import com.zero.bwtableback.restaurant.dto.*;
import com.zero.bwtableback.restaurant.entity.ReservationSetting;
import com.zero.bwtableback.restaurant.entity.TimeslotSetting;
import com.zero.bwtableback.restaurant.entity.WeekdaySetting;
import com.zero.bwtableback.restaurant.repository.ReservationSettingRepository;
import com.zero.bwtableback.restaurant.repository.TimeslotSettingRepository;
import com.zero.bwtableback.restaurant.repository.WeekdaySettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationSettingService {

    private final ReservationSettingRepository reservationSettingRepository;
    private final WeekdaySettingRepository weekdaySettingRepository;
    private final TimeslotSettingRepository timeslotSettingRepository;

    // 예약 설정 등록
    public ReservationSettingResDto createReservationSetting(ReservationSettingReqDto reqDto) {

        // 기간 겹치는지 체크
        boolean isOverlap = reservationSettingRepository.existsByRestaurantIdAndOverlappingDates(
                reqDto.getRestaurantId(), reqDto.getStartDate(), reqDto.getEndDate());

        if (isOverlap) {
            throw new IllegalArgumentException("Reservation setting period must not be overlapped");
        }

        // 예약 설정 생성
        ReservationSetting reservationSetting = ReservationSetting.builder()
                .startDate(reqDto.getStartDate())
                .endDate(reqDto.getEndDate())
                .restaurantId(reqDto.getRestaurantId())
                .build();

        reservationSetting = reservationSettingRepository.save(reservationSetting);

        // 요일 설정
        for (WeekdaySettingDto weekdaySettingDto: reqDto.getWeekdaySettings()) {
            WeekdaySetting weekdaySetting = WeekdaySetting.builder()
                    .dayOfWeek(weekdaySettingDto.getDayOfWeek())
                    .reservationSetting(reservationSetting)
                    .build();

            weekdaySetting = weekdaySettingRepository.save(weekdaySetting);

            for (TimeslotSettingDto timeslotSettingDto: weekdaySettingDto.getTimeslotSettings()) {
                TimeslotSetting timeslotSetting = TimeslotSetting.builder()
                        .timeslot(timeslotSettingDto.getTimeslot())
                        .maxCapacity(timeslotSettingDto.getMaxCapacity())
                        .weekdaySetting(weekdaySetting)
                        .build();

                timeslotSettingRepository.save(timeslotSetting);
            }
        }

        return ReservationSettingResDto.builder()
                .id(reservationSetting.getId())
                .restaurantId(reqDto.getRestaurantId())
                .message("ReservationSetting created successfully")
                .build();
    }

    // 특정 레스토랑의 모든 예약 설정 조회
    public List<ReservationSettingDetailDto> getReservationSettingByRestaurantId(Long restaurantId) {

        List<ReservationSetting> reservationSettings = reservationSettingRepository.findByRestaurantId(restaurantId);

        return reservationSettings.stream()
                .map(this::getReservationSettingDetail)
                .collect(Collectors.toList());
    }

    // 예약설정 id로 예약 설정 조회
    public ReservationSettingDetailDto getReservationSettingById(Long id) {
        ReservationSetting reservationSetting = reservationSettingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reservation setting not found"));

        return getReservationSettingDetail(reservationSetting);
    }

    // 예약 설정 조회 시 상세 정보 반환에 필요한 공통 메서드
    private ReservationSettingDetailDto getReservationSettingDetail(ReservationSetting reservationSetting) {

        List<WeekdaySetting> weekdaySettings = weekdaySettingRepository.findByReservationSetting(reservationSetting);

        List<WeekdaySettingDto> weekdaySettingDtos = weekdaySettings.stream()
                .map(weekdaySetting -> {
                    List<TimeslotSetting> timeslotSettings = timeslotSettingRepository.findByWeekdaySetting(weekdaySetting);

                    List<TimeslotSettingDto> timeslotSettingDtos = timeslotSettings.stream()
                            .map(timeslotSetting -> TimeslotSettingDto.builder()
                                    .timeslot(timeslotSetting.getTimeslot())
                                    .maxCapacity(timeslotSetting.getMaxCapacity())
                                    .build())
                            .collect(Collectors.toList());

                    return WeekdaySettingDto.builder()
                            .dayOfWeek(weekdaySetting.getDayOfWeek())
                            .timeslotSettings(timeslotSettingDtos)
                            .build();
                }).collect(Collectors.toList());

        return ReservationSettingDetailDto.builder()
                .id(reservationSetting.getId())
                .restaurantId(reservationSetting.getRestaurantId())
                .startDate(reservationSetting.getStartDate())
                .endDate(reservationSetting.getEndDate())
                .weekdaySettings(weekdaySettingDtos)
                .build();
    }

    // 예약 설정 삭제
    @Transactional
    public void deleteReservationSetting(Long id) {
        ReservationSetting reservationSetting = reservationSettingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ReservationSetting not found"));

        List<WeekdaySetting> weekdaySettings = reservationSetting.getWeekdaySettings();
        if (!weekdaySettings.isEmpty()) {
            timeslotSettingRepository.deleteByWeekdaySettingIn(weekdaySettings);
        }

        weekdaySettingRepository.deleteAll(weekdaySettings);

        reservationSettingRepository.delete(reservationSetting);
    }

}
