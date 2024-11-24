package com.zero.bwtableback.statistics.util;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

public class DateRangeCalculator {

    public static LocalDate getEndDateForMonthlyPeriod(LocalDate today) {
        if (isLastDayOfMonth(today)) {
            return today;
        }
        return getLastDayOfPreviousMonth(today);
    }

    public static LocalDate getStartDateSixMonthsAgo(LocalDate date) {
        return date.minusMonths(6).with(TemporalAdjusters.firstDayOfMonth());
    }

    public static LocalDate getStartOfWeek(LocalDate currentDate, int weeksAgo) {
        LocalDate targetDate = currentDate.minusWeeks(weeksAgo);
        return targetDate.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.SUNDAY));
    }

    public static LocalDate getEndDateOfWeek(LocalDate currentDate) {
        return currentDate.with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SATURDAY));
    }

    private static LocalDate getLastDayOfPreviousMonth(LocalDate date) {
        return date.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
    }

    private static boolean isLastDayOfMonth(LocalDate date) {
        return date.equals(date.with(TemporalAdjusters.lastDayOfMonth()));
    }

}
