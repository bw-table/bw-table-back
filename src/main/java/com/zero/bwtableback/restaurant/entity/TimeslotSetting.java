package com.zero.bwtableback.restaurant.entity;

import jakarta.persistence.*;

import java.time.LocalTime;

@Entity
@Table(name = "timeslot_setting")
public class TimeslotSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalTime timeslot;

    @Column(nullable = false)
    private int maxCapacity; // 시간대별 최대 예약 가능 인원수

    @ManyToOne
    @JoinColumn(name = "weekday_setting_id", nullable = false)
    private WeekdaySetting weekdaySetting;
}
