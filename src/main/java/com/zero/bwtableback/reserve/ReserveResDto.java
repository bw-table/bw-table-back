package com.zero.bwtableback.reserve;

import com.zero.bwtableback.reservation.entity.ReservationStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReserveResDto {
    private String date; // 예약 날짜
    private String time; // 예약 시간
    private Integer people; // 인원 수
    private ReservationStatus status;

    public ReserveResDto(Reserve reservation) {
        this.date = reservation.getDate();
        this.time = reservation.getTime();
        this.people = reservation.getPeople();
        this.status = reservation.getStatus();
    }
}
