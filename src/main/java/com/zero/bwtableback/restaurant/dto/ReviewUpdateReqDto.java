package com.zero.bwtableback.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ReviewUpdateReqDto {

    private String content;
    private Integer rating;
    private List<Long> imageIdsToDelete; // 삭제할 이미지 id 리스트
}