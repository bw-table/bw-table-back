package com.zero.bwtableback.reservation.dto;

import com.zero.bwtableback.reservation.entity.Reservation;
import com.zero.bwtableback.restaurant.entity.Hashtag;
import com.zero.bwtableback.restaurant.entity.Menu;
import com.zero.bwtableback.restaurant.entity.Restaurant;
import com.zero.bwtableback.restaurant.entity.RestaurantImage;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

public record PaymentCompleteDto(
        ReservationInfo reservation,
        RestaurantInfo restaurant
) {
    public static PaymentCompleteDto fromEntities(Restaurant restaurant, Reservation reservation) {
        return new PaymentCompleteDto(
                new ReservationInfo(
                        reservation.getId(),
                        reservation.getReservationDate(),
                        reservation.getReservationTime(),
                        reservation.getNumberOfPeople(),
                        reservation.getSpecialRequest()
                ),
                new RestaurantInfo(
                        restaurant.getId(),
                        restaurant.getName(),
                        restaurant.getDescription(),
                        restaurant.getAddress(),
                        restaurant.getContact(),
                        restaurant.getClosedDay(),
                        restaurant.getOperatingHours().toString(),
                        restaurant.getImages().stream()
                                .map(RestaurantImage::getImageUrl)
                                .collect(Collectors.toList()),
                        restaurant.getCategory().getCategoryType().name(),
                        restaurant.getMenus().stream()
                                .map(Menu::getName)
                                .collect(Collectors.toList()),
                        restaurant.getFacilities().stream()
                                .map(facility -> facility.getFacilityType().name())
                                .collect(Collectors.toList()),
                        restaurant.getHashtags().stream()
                                .map(Hashtag::getName)
                                .collect(Collectors.toList())
                )
        );
    }

    record ReservationInfo(
            Long reservationId,
            LocalDate reservationDate,
            LocalTime reservationTime,
            int numberOfPeople,
            String specialRequest
    ) {
    }

    record RestaurantInfo(
            Long restaurantId,
            String name,
            String description,
            String location,
            String phone,
            String regularHoliday,
            String operatingHours,
            List<String> images,
            String category,
            List<String> menus,
            List<String> facilities,
            List<String> hashtags
    ) {
    }
}
