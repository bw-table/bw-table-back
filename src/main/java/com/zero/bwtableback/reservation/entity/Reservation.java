package com.zero.bwtableback.reservation.entity;

import static com.zero.bwtableback.common.exception.ErrorCode.CUSTOMER_CANCEL_TOO_LATE;
import static com.zero.bwtableback.common.exception.ErrorCode.INVALID_STATUS_CONFIRM;
import static com.zero.bwtableback.common.exception.ErrorCode.INVALID_STATUS_CUSTOMER_CANCEL;
import static com.zero.bwtableback.common.exception.ErrorCode.INVALID_STATUS_NO_SHOW;
import static com.zero.bwtableback.common.exception.ErrorCode.INVALID_STATUS_OWNER_CANCEL;
import static com.zero.bwtableback.common.exception.ErrorCode.INVALID_STATUS_VISITED;

import com.zero.bwtableback.common.BaseEntity;
import com.zero.bwtableback.common.exception.CustomException;
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
import java.time.temporal.ChronoUnit;
import java.util.List;
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


    public void confirmReservation() {
        if (this.reservationStatus == ReservationStatus.CONFIRMED ||
                this.reservationStatus == ReservationStatus.CUSTOMER_CANCELED ||
                this.reservationStatus == ReservationStatus.OWNER_CANCELED ||
                this.reservationStatus == ReservationStatus.NO_SHOW ||
                this.reservationStatus == ReservationStatus.VISITED) {
            throw new CustomException(INVALID_STATUS_CONFIRM);
        }
        this.reservationStatus = ReservationStatus.CONFIRMED;
    }

    public void cancelByCustomer() {
        if (this.reservationStatus != ReservationStatus.CONFIRMED) {
            throw new CustomException(INVALID_STATUS_CUSTOMER_CANCEL);
        }
        if (ChronoUnit.DAYS.between(reservationDate, LocalDate.now()) > 3) {
            throw new CustomException(CUSTOMER_CANCEL_TOO_LATE);
        }
        this.reservationStatus = ReservationStatus.CUSTOMER_CANCELED;
    }

    public void cancelByOwner() {
        if (this.reservationStatus != ReservationStatus.CONFIRMED) {
            throw new CustomException(INVALID_STATUS_OWNER_CANCEL);
        }
        this.reservationStatus = ReservationStatus.OWNER_CANCELED;
    }

    public void markAsNoShow() {
        if (this.reservationStatus != ReservationStatus.CONFIRMED) {
            throw new CustomException(INVALID_STATUS_NO_SHOW);
        }
        this.reservationStatus = ReservationStatus.NO_SHOW;
    }

    public void markAsVisited() {
        if (this.reservationStatus != ReservationStatus.CONFIRMED) {
            throw new CustomException(INVALID_STATUS_VISITED);
        }
        this.reservationStatus = ReservationStatus.VISITED;

    }

}
