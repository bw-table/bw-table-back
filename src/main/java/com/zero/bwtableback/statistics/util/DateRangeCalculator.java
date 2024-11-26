package com.zero.bwtableback.statistics.util;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

public class DateRangeCalculator {

    public static LocalDate getEndDateForMonthRange(LocalDate today) {
        if (isLastDayOfMonth(today)) {
            return today;
        }
        return getLastDayOfPreviousMonth(today);
    }

    public static LocalDate getStartDateForMonthRange(LocalDate date) {
        return date.minusMonths(6).with(TemporalAdjusters.firstDayOfMonth());
    }

    public static LocalDate getStartOfWeekRange(LocalDate endDate, int weeksAgo) {
        return endDate.minusWeeks(weeksAgo).with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.SUNDAY));
    }

    public static LocalDate getEndOfWeekRange(LocalDate currentDate) {
        return currentDate.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.SATURDAY));
    }

    private static LocalDate getLastDayOfPreviousMonth(LocalDate date) {
        return date.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
    }

    private static boolean isLastDayOfMonth(LocalDate date) {
        return date.equals(date.with(TemporalAdjusters.lastDayOfMonth()));
    }

}
