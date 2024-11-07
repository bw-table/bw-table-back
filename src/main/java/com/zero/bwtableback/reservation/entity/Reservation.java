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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus reservationStatus;

    // Payment, Notification 엔티티 미작성으로 주석 처리
//    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL)
//    private List<Payment> payments;
//
//    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL)
//    private List<Notification> notifications;


    // TODO: 예약 상태 변경 메서드 커스텀 예외 처리 필요

    public void confirmReservation() {
        if (this.reservationStatus == ReservationStatus.CONFIRMED) {
            throw new IllegalStateException("이미 확정된 예약입니다.");
        }
        this.reservationStatus = ReservationStatus.CONFIRMED;
    }

    public void cancelByCustomer() {
        if (this.reservationStatus == ReservationStatus.CONFIRMED) {
            this.reservationStatus = ReservationStatus.CUSTOMER_CANCELED;
        } else {
            throw new IllegalStateException("고객 취소는 확정된 예약에 대해서만 가능합니다.");
        }
    }

    public void cancelByOwner() {
        if (this.reservationStatus == ReservationStatus.CONFIRMED) {
            this.reservationStatus = ReservationStatus.OWNER_CANCELED;
        } else {
            throw new IllegalStateException("가게 취소는 확정된 예약에 대해서만 가능합니다.");
        }
    }

    public void markAsNoShow() {
        if (this.reservationStatus == ReservationStatus.CONFIRMED) {
            this.reservationStatus = ReservationStatus.NO_SHOW;
        } else {
            throw new IllegalStateException("노쇼 처리할 수 없는 상태입니다.");
        }
    }

    public void markAsVisited() {
        if (this.reservationStatus == ReservationStatus.CONFIRMED) {
            this.reservationStatus = ReservationStatus.VISITED;
        } else {
            throw new IllegalStateException("방문 완료로 변경할 수 없는 상태입니다.");
        }
    }

}
