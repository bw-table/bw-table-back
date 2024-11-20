package com.zero.bwtableback.payment.entity;

import com.zero.bwtableback.reservation.entity.Reservation;
import com.zero.bwtableback.restaurant.entity.Restaurant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "payment")
public class PaymentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 결제 ID

    private String impUid; // 아임포트에서 받은 결제 고유 번호
    private String merchantUid; // 주문 ID
    private String buyerName; // 구매자 이름
    private String buyerEmail; // 구매자 이메일
    private String buyerTel; // 구매자 전화번호
    private String cardName; // 카드 이름
    private String cardNumber; // 카드 번호
    private BigDecimal paidAmount; // 결제 금액
    private String currency; // 통화

    @Enumerated(EnumType.STRING)
    private PaymentStatus status; // 결제 상태 (예: PAID, REFUNDED, CANCELED)

    private Date paidAt; // 결제 완료 시간

    @Column(name = "receipt_url")
    private String receiptUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;
}
