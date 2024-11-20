package com.zero.bwtableback.reservation.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentReqDto {
    private String reservationToken; // 예약 토큰
//    private String applyNum;      // 신청 번호
//    private String bankName;      // 은행 이름 (null 가능)
//    private String buyerAddr;     // 구매자 주소 (빈 문자열 가능)
//    private String buyerEmail;    // 구매자 이메일
//    private String buyerName;     // 구매자 이름
//    private String buyerPostcode; // 구매자 우편번호 (빈 문자열 가능)
//    private String buyerTel;      // 구매자 전화번호
//    private String cardName;      // 카드 이름
//    private String cardNumber;    // 카드 번호 (마스킹 처리)
//    private int cardQuota;        // 카드 할부 개월 수
//    private String currency;       // 통화 (예: "KRW")
//    private String customData;     // 커스텀 데이터 (null 가능)
    private String impUid;        // 결제 고유 ID
//    private String merchantUid;   // 가맹점 고유 ID
//    private String name;          // 상품명
//    private int paidAmount;       // 결제 금액
//    private long paidAt;          // 결제 시간 (Unix timestamp)
//    private String payMethod;     // 결제 방법
//    private String pgProvider;    // PG 제공자 (예: "html5_inicis")
//    private String pgTid;         // PG 거래 ID
//    private String pgType;        // PG 타입 (예: "payment")
//    private String receiptUrl;    // 영수증 URL
//    private String status;        // 결제 상태 (예: "paid")
//    private boolean success;      // 결제 성공 여부
}