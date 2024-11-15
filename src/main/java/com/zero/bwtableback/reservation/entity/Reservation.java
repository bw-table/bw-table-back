package com.zero.bwtableback.reservation.entity;

import com.zero.bwtableback.common.BaseEntity;
import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.restaurant.entity.Restaurant;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Reservation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(updatable = false, nullable = false)
    private Restaurant restaurant;

    @ManyToOne
    @JoinColumn(updatable = false, nullable = false)
    private Member member;

    @Column(nullable = false)
    private LocalDate reservationDate;

    @Column(nullable = false)
    private LocalTime reservationTime;

    @Column(nullable = false)
    private int numberOfPeople;

    private String specialRequest;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus reservationStatus;

    // Payment, Notification 엔티티 미작성으로 주석 처리
//    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL)
//    private List<Payment> payments;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL)
    private List<Notification> notifications;

}
