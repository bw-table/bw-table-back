package com.zero.bwtableback.restaurant.controller;

import com.zero.bwtableback.restaurant.dto.ReviewInfoDto;
import com.zero.bwtableback.restaurant.dto.ReviewReqDto;
import com.zero.bwtableback.restaurant.dto.ReviewResDto;
import com.zero.bwtableback.restaurant.dto.ReviewUpdateReqDto;
import com.zero.bwtableback.restaurant.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {

    private final ReviewService reviewService;

    // 리뷰 작성
    @PostMapping("/{restaurantId}/review/new")
    public ResponseEntity<ReviewResDto> createReview(@PathVariable Long restaurantId,
                                                     @RequestPart(value = "review") ReviewReqDto reqDto,
                                                     @RequestPart(value = "images", required = false) MultipartFile[] images) throws IOException {
//        reqDto = new ReviewReqDto(reqDto.getContent(), reqDto.getRating(), images);

        // 이미지 배열이 제대로 전달됐는지 확인
        if (images != null && images.length > 0) {
            log.info("Number of images uploaded: " + images.length);
        } else {
            log.warn("No images uploaded.");
        }

        ReviewResDto resDto = reviewService.createReview(restaurantId, reqDto, images);

        return ResponseEntity.ok(resDto);
    }

    // 리뷰 수정
    @PutMapping("/{restaurantId}/reviews/{reviewId}")
    public ResponseEntity<ReviewResDto> updateReview(@PathVariable Long restaurantId,
                                                     @PathVariable Long reviewId,
                                                     @RequestPart(value = "review") ReviewUpdateReqDto reqDto,
                                                     @RequestPart(value = "images", required = false) MultipartFile[] images) throws IOException {
        ReviewResDto response = reviewService.updateReview(reviewId, restaurantId, reqDto, images);

        return ResponseEntity.ok(response);
    }

    // 리뷰 삭제
    @DeleteMapping("/{restaurantId}/reviews/{reviewId}")
    public ResponseEntity<String> deleteReview(@PathVariable Long restaurantId,
                                               @PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId, restaurantId);

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
