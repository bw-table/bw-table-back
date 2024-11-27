package com.zero.bwtableback.statistics.util;

import java.time.LocalDate;

public class TimeKeyGenerator {

    public static String generateDailyKey(LocalDate date) {
        return date.toString(); // "YYYY-MM-DD"
    }

    public static String generateWeeklyKey(LocalDate startOfWeek, LocalDate endOfWeek) {
        return startOfWeek + " ~ " + endOfWeek; // "2024-11-17 ~ 2024-11-23"
    }

    public static String generateMonthlyKey(LocalDate date) {
        return date.getYear() + "-" + String.format("%02d", date.getMonthValue()); // "YYYY-MM"
    }

}
