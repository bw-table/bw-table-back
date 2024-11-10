package com.zero.bwtableback.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ReviewUpdateReqDto {

    private String content;
    private Integer rating;
    private List<String> images;
}
