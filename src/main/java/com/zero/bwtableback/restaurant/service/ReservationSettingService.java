package com.zero.bwtableback.restaurant.service;

import com.zero.bwtableback.restaurant.dto.ReservationSettingReqDto;
import com.zero.bwtableback.restaurant.dto.ReservationSettingResDto;
import com.zero.bwtableback.restaurant.dto.TimeslotSettingDto;
import com.zero.bwtableback.restaurant.entity.DayOfWeek;
import com.zero.bwtableback.restaurant.entity.ReservationSetting;
import com.zero.bwtableback.restaurant.entity.TimeslotSetting;
import com.zero.bwtableback.restaurant.entity.WeekdaySetting;
import com.zero.bwtableback.restaurant.repository.ReservationSettingRepository;
import com.zero.bwtableback.restaurant.repository.TimeslotSettingRepository;
import com.zero.bwtableback.restaurant.repository.WeekdaySettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ReservationSettingService {

    private final ReservationSettingRepository reservationSettingRepository;
    private final WeekdaySettingRepository weekdaySettingRepository;
    private final TimeslotSettingRepository timeslotSettingRepository;

    // 예약 설정 등록
    public ReservationSettingResDto createReservationSetting(ReservationSettingReqDto reqDto) {

        ReservationSetting reservationSetting = ReservationSetting.builder()
                .startDate(reqDto.getStartDate())
                .endDate(reqDto.getEndDate())
                .restaurantId(reqDto.getRestaurantId())
                .build();

        reservationSetting = reservationSettingRepository.save(reservationSetting);

        // 요일 설정
        for (DayOfWeek weekday: reqDto.getWeekdays()) {
            WeekdaySetting weekdaySetting = WeekdaySetting.builder()
                    .dayOfWeek(weekday)
                    .reservationSetting(reservationSetting)
                    .build();

            weekdaySettingRepository.save(weekdaySetting);

            for (TimeslotSettingDto timeslot: reqDto.getTimeslotSettings()) {
                TimeslotSetting timeslotSetting = TimeslotSetting.builder()
                        .timeslot(timeslot.getTimeslot())
                        .maxCapacity(timeslot.getMaxCapacity())
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
}
