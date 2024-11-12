package com.zero.bwtableback.restaurant.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Builder
@Getter
@Entity
@Table(name = "reservation_setting")
public class ReservationSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate startDate;
    private LocalDate endDate;

    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId; // 외래키

    @ManyToOne
    @JoinColumn(name = "restaurant_id", insertable = false, updatable = false)
    private Restaurant restaurant; // 연관된 Restaurant 객체 조회용
}
