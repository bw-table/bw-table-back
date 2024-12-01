package com.zero.bwtableback.reservation.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentReqDto {
    private String reservationToken; // 예약 토큰
    private String impUid;        // 결제 고유 ID
}