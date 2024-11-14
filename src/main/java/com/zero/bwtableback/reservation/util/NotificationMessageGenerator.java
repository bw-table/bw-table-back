package com.zero.bwtableback.reservation.util;

import com.zero.bwtableback.common.exception.CustomException;
import com.zero.bwtableback.common.exception.ErrorCode;
import com.zero.bwtableback.reservation.entity.NotificationType;
import com.zero.bwtableback.reservation.entity.Reservation;
import java.time.LocalDate;
import java.time.LocalTime;

public class NotificationMessageGenerator {

    public static String generateMessage(Reservation reservation, NotificationType type) {
        String customerName = reservation.getMember().getName();
        String restaurantName = reservation.getRestaurant().getName();
        LocalDate reservationDate = reservation.getReservationDate();
        LocalTime reservationTime = reservation.getReservationTime();

        return switch (type) {
            case CONFIRMATION -> String.format("예약이 확정되었습니다! %s에 %s님이 %s %s에 방문 예정입니다.",
                    restaurantName, customerName, reservationDate, reservationTime);
            case CANCELLATION -> String.format("예약 취소 안내드립니다. %s에서 %s님의 %s %s 예약이 취소되었습니다.",
                    restaurantName, customerName, reservationDate, reservationTime);
            case REMINDER_24H -> String.format("예약 리마인더: %s에서 %s님의 %s %s 예약이 24시간 남았습니다.",
                    restaurantName, customerName, reservationDate, reservationTime);
            case DAY_OF_VISIT -> String.format("오늘 예약이 있습니다! %s에서 %s님이 %s에 방문 예정입니다.",
                    restaurantName, customerName, reservationTime);
            default -> throw new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND);
        };
    }
}