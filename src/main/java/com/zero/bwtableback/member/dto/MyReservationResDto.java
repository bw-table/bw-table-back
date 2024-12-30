package com.zero.bwtableback.member.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zero.bwtableback.reservation.entity.Reservation;
import com.zero.bwtableback.reservation.entity.ReservationStatus;
import com.zero.bwtableback.restaurant.entity.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MyReservationResDto {
    private Long reservationId;
    private Long restaurantId;
    private String restaurantName;
    private CategoryType restaurantCategory;
    private List<String> restaurantImages;
    private Long memberId;
    private String nickname;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate reservationDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime reservationTime;
    private int numberOfPeople;
    private String specialRequest;
    private ReservationStatus reservationStatus;

    public static MyReservationResDto fromEntity(Reservation reservation) {
        return MyReservationResDto.builder()
                .reservationId(reservation.getId())
                .restaurantId(reservation.getRestaurant().getId())
                .restaurantName(reservation.getRestaurant().getName())
                .restaurantCategory(reservation.getRestaurant().getCategory().getCategoryType())
                .restaurantImages(extractImageUrls(reservation.getRestaurant().getImages()))
                .memberId(reservation.getMember().getId())
                .nickname(reservation.getMember().getNickname())
                .reservationDate(reservation.getReservationDate())
                .reservationTime(reservation.getReservationTime())
                .numberOfPeople(reservation.getNumberOfPeople())
                .specialRequest(reservation.getSpecialRequest())
                .reservationStatus(reservation.getReservationStatus())
                .build();
    }

    private static List<String> extractImageUrls(Set<RestaurantImage> images) {
        return images.stream()
                .map(RestaurantImage::getImageUrl)
                .collect(Collectors.toList());
    }
}
