package com.zero.bwtableback.payment.entity;

import com.zero.bwtableback.reserve.Reserve;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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
    private String cardNumber; // 카드 번호 (마스킹 처림 된 번호)

    private Integer paidAmount; // 결제 금액 (정수형)

    private String currency; // 통화 (예: KRW)

    @Enumerated(EnumType.STRING)
    private PaymentStatus status; // 결제 상태 (예: PAID, REFUNDED, CANCELED)

    private Long paidAt; // 결제 완료 시간 (Unix timestamp)

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "reservation_id")
    private Reserve reservation; // 예약 정보와의 관계

    @Column(name = "receipt_url")
    private String receiptUrl; // 영수증 URL
}
