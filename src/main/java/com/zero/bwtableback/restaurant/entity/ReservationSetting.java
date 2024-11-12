package com.zero.bwtableback.restaurant.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "reservation_setting")
public class ReservationSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate startDate;
    private LocalDate endDate;

    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;
}
