package com.zero.bwtableback.statistics.entity;

public enum StatisticsType {
    DAILY,          // 일별 통계: 하루 단위로 예약 건수를 집계
    WEEKLY,         // 주별 통계: 한 주(일요일~토요일) 단위로 예약 건수를 집계
    MONTHLY,        // 월별 통계: 한 달 단위로 예약 건수를 집계
    TIME_SLOT,      // 인기 시간대 통계: 시간대 단위로 예약 건수를 집계
    POPULAR_DATE    // 인기 날짜 통계: 특정 기간 내 예약이 많았던 상위 일자 5개 집계
}
