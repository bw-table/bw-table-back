package com.zero.bwtableback.restaurant.dto;

import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
public class MenuUpdateDto {

    private Long id;
    private String name;
    private Integer price;
    private String description;
    private MultipartFile image; // 새로운 이미지
    private Boolean deleteImage; // 이미지 삭제할지 여부
}
