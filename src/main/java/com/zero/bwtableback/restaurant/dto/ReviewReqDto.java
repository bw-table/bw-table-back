package com.zero.bwtableback.restaurant.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ReviewReqDto {

    @NotNull(message = "Review content must not be null")
    private String content;

    @NotNull(message = "Rating must not be null")
    private int rating;

    private List<String> images;
}
