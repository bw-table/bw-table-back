package com.zero.bwtableback.restaurant.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "reservation_setting")
public class ReservationSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate startDate;
    private LocalDate endDate;

    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;

    @ManyToOne
    @JoinColumn(name = "restaurant_id", insertable = false, updatable = false)
    private Restaurant restaurant;

    @OneToMany(mappedBy = "reservationSetting", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WeekdaySetting> weekdaySettings = new ArrayList<>();
}
