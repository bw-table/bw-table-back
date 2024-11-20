package com.zero.bwtableback.restaurant.controller;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.restaurant.dto.ReviewInfoDto;
import com.zero.bwtableback.restaurant.dto.ReviewReqDto;
import com.zero.bwtableback.restaurant.dto.ReviewResDto;
import com.zero.bwtableback.restaurant.dto.ReviewUpdateReqDto;
import com.zero.bwtableback.restaurant.service.ReviewService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    @PreAuthorize("hasRole('GUEST')")
    @PostMapping("/{restaurantId}/review/new")
    public ResponseEntity<ReviewResDto> createReview(@PathVariable Long restaurantId,
                                                     @RequestPart(value = "review") ReviewReqDto reqDto,
                                                     @RequestPart(value = "images", required = false) MultipartFile[] images,
                                                     @AuthenticationPrincipal Member member) throws IOException {
//        reqDto = new ReviewReqDto(reqDto.getContent(), reqDto.getRating(), images);

        // 이미지 배열이 제대로 전달됐는지 확인
        if (images != null && images.length > 0) {
            log.info("Number of images uploaded: " + images.length);
        } else {
            log.warn("No images uploaded.");
        }

        ReviewResDto resDto = reviewService.createReview(restaurantId, reqDto, images, member);

        return ResponseEntity.ok(resDto);
    }

    // 리뷰 수정
    @PreAuthorize("hasRole('GUEST')")
    @PutMapping("/{restaurantId}/reviews/{reviewId}")
    public ResponseEntity<ReviewResDto> updateReview(@PathVariable Long restaurantId,
                                                     @PathVariable Long reviewId,
                                                     @RequestPart(value = "review") ReviewUpdateReqDto reqDto,
                                                     @RequestPart(value = "images", required = false) MultipartFile[] images,
                                                     @AuthenticationPrincipal Member member) throws IOException {

        ReviewResDto response = reviewService.updateReview(reviewId, restaurantId, reqDto, images, member);

        return ResponseEntity.ok(response);
    }

    // 리뷰 삭제
    @PreAuthorize("hasRole('GUEST')")
    @DeleteMapping("/{restaurantId}/reviews/{reviewId}")
    public ResponseEntity<String> deleteReview(@PathVariable Long restaurantId,
                                               @PathVariable Long reviewId,
                                               @AuthenticationPrincipal Member member) {
        try {
            return reviewService.deleteReview(reviewId, restaurantId, member);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Review or Restaurant not found");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error occurred during deleting review");
        }
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
