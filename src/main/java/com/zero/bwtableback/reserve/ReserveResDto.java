package com.zero.bwtableback.reserve;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReserveResDto {
    private Long id; // 예약 ID
    private String date; // 예약 날짜
    private String time; // 예약 시간
    private Integer people; // 인원 수

    public ReserveResDto(Reserve reservation) {
        this.id = reservation.getId();
        this.date = reservation.getDate();
        this.time = reservation.getTime();
        this.people = reservation.getPeople();
    }
}
