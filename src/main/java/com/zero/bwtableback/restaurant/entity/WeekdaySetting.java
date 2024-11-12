package com.zero.bwtableback.restaurant.entity;

import jakarta.persistence.*;
import lombok.Builder;

@Builder
@Entity
@Table(name = "weekday_setting")
public class WeekdaySetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayOfWeek dayOfWeek; // 요일

    @ManyToOne
    @JoinColumn(name = "reservation_setting_id", nullable = false)
    private ReservationSetting reservationSetting;
}
