package com.zero.bwtableback.restaurant.controller;

import com.zero.bwtableback.restaurant.dto.ReviewInfoDto;
import com.zero.bwtableback.restaurant.dto.ReviewReqDto;
import com.zero.bwtableback.restaurant.dto.ReviewResDto;
import com.zero.bwtableback.restaurant.dto.ReviewUpdateReqDto;
import com.zero.bwtableback.restaurant.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // 리뷰 작성
    @PostMapping("/{restaurantId}/review/new")
    public ResponseEntity<ReviewResDto> createReview(@PathVariable Long restaurantId,
                                                     @RequestBody @Valid ReviewReqDto reqDto) {
        ReviewResDto resDto = reviewService.createReview(restaurantId, reqDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(resDto);
    }

    // 리뷰 수정
    @PutMapping("/review/{id}")
    public ResponseEntity<ReviewResDto> updateReview(@PathVariable Long id,
                                                     @RequestParam Long restaurantId,
                                                     @RequestBody ReviewUpdateReqDto reqDto) {
        ReviewResDto response = reviewService.updateReview(id, restaurantId, reqDto);

        return ResponseEntity.ok(response);
    }

    // 리뷰 삭제
    @DeleteMapping("/review/{id}")
    public ResponseEntity<String> deleteReview(@PathVariable Long id,
                                               @RequestParam Long restaurantId) {
        reviewService.deleteReview(id, restaurantId);

        return ResponseEntity.ok("Review deleted successfully");
    }

    // 식당 리뷰 목록 조회
    @GetMapping("/{restaurantId}/reviews")
    public ResponseEntity<List<ReviewInfoDto>> getReviewsByRestaurant(@PathVariable Long restaurantId,
                                                                      Pageable pageable) {
        List<ReviewInfoDto> reviews = reviewService.getReviewsByRestaurant(restaurantId, pageable);
        return ResponseEntity.ok(reviews);
    }

    // 리뷰 상세 조회
    @GetMapping("/reviews/{id}")
    public ResponseEntity<ReviewInfoDto> getReviewById(@PathVariable Long id) {
        ReviewInfoDto reviewInfo = reviewService.getReviewById(id);

        return ResponseEntity.ok(reviewInfo);
    }

}
