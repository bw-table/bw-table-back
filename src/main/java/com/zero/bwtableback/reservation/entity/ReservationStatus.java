package com.zero.bwtableback.reservation.entity;

public enum ReservationStatus {
    CONFIRMED, // 예약 확정
    CUSTOMER_CANCELED, // 예약 취소
    OWNER_CANCELED, // 예약 취소
    NO_SHOW, // 노쇼
    VISITED // 방문 완료
}
