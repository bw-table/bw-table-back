package com.zero.bwtableback.reservation.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReservationCreateResDto {
    String reservationToken;
    String name;
    int amount;
    String buyerEmail;
    String buyerName;
    String buyerTel;
}
