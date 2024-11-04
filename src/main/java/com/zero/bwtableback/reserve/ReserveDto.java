package com.zero.bwtableback.reserve;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReserveDto {
    private String date; // 예약 날짜
    private String time; // 예약 시간
    private Integer people; // 인원 수
}
