package com.zero.bwtableback.restaurant.controller;

import com.zero.bwtableback.restaurant.dto.ReviewReqDto;
import com.zero.bwtableback.restaurant.dto.ReviewResDto;
import com.zero.bwtableback.restaurant.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // 리뷰 작성
    @PostMapping("/{restaurantId}/new")
    public ResponseEntity<ReviewResDto> createReview(
                                            @PathVariable Long restaurantId,
                                            @RequestBody @Valid ReviewReqDto reqDto) {
        System.out.println("createReview 컨트롤러 호출");
        ReviewResDto review = reviewService.createReview(restaurantId, reqDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(review);
    }

    // 식당 리뷰 목록 조회
    @GetMapping("/{restaurantId}")
    public ResponseEntity<List<ReviewResDto>> getReviewsByRestaurant(
                                            @PathVariable Long restaurantId) {
        List<ReviewResDto> reviews = reviewService.getReviewsByRestaurant(restaurantId);
        return ResponseEntity.ok(reviews);
    }

}
