package com.zero.bwtableback.statistics.util;

import java.time.LocalDate;
import java.time.temporal.WeekFields;

public class PeriodKeyGenerator {

    public static String generateDailyKey(LocalDate date) {
        return date.toString(); // "YYYY-MM-DD"
    }

    public static String generateWeeklyKey(LocalDate date) {
        return date.getYear() + "-W" + getWeekOfYear(date); // "YYYY-Wxx"
    }

    public static String generateMonthlyKey(LocalDate date) {
        return date.getYear() + "-" + String.format("%02d", date.getMonthValue()); // "YYYY-MM"
    }

    private static int getWeekOfYear(LocalDate date) {
        return date.get(WeekFields.ISO.weekOfYear());
    }

}

