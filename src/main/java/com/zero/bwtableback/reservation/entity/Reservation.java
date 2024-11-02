package com.zero.bwtableback.reservation.entity;

import com.zero.bwtableback.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Reservation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @ManyToOne
//    @JoinColumn(updatable = false, nullable = false)
//    private Restaurant restaurant;
//
//    @ManyToOne
//    @JoinColumn(updatable = false, nullable = false)
//    private User user;

    @Column(nullable = false)
    private LocalDate reservationDate;

    @Column(nullable = false)
    private LocalTime reservationTime;

    @Column(nullable = false)
    private int numberOfPeople;

    private String specialRequests;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus reservationStatus;

//    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL)
//    private List<Payment> payments;
//
//    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL)
//    private List<Notification> notifications;

}
