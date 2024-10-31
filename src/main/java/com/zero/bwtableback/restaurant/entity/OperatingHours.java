package com.zero.bwtableback.restaurant.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "operating_hours")
public class OperatingHours {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String dayOfWeek; // 요일

    private LocalTime openingTime;

    private LocalTime closingTime;

    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;
}
