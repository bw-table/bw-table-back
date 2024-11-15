package com.zero.bwtableback.restaurant.service;

import com.zero.bwtableback.common.service.ImageUploadService;
import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.member.repository.MemberRepository;
import com.zero.bwtableback.reservation.entity.Reservation;
import com.zero.bwtableback.reservation.entity.ReservationStatus;
import com.zero.bwtableback.reservation.repository.ReservationRepository;
import com.zero.bwtableback.restaurant.dto.ReviewInfoDto;
import com.zero.bwtableback.restaurant.dto.ReviewUpdateReqDto;
import com.zero.bwtableback.restaurant.entity.Restaurant;
import com.zero.bwtableback.restaurant.entity.ReviewImage;
import com.zero.bwtableback.restaurant.repository.RestaurantRepository;
import com.zero.bwtableback.restaurant.dto.ReviewReqDto;
import com.zero.bwtableback.restaurant.dto.ReviewResDto;
import com.zero.bwtableback.restaurant.entity.Review;
import com.zero.bwtableback.restaurant.repository.ReviewImageRepository;
import com.zero.bwtableback.restaurant.repository.ReviewRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final RestaurantRepository restaurantRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final ImageUploadService imageUploadService;
    private final MemberRepository memberRepository;
    private final ReservationRepository reservationRepository;

    // 리뷰 작성
    public ReviewResDto createReview(Long restaurantId, ReviewReqDto reqDto, MultipartFile[] images) throws IOException {

        Member member = memberRepository.findById(reqDto.getMemberId())
                .orElseThrow(() -> new EntityNotFoundException("Member not found with id: " + reqDto.getMemberId()));

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found with id: " + restaurantId));

        Reservation reservation = reservationRepository.findByMemberAndRestaurantAndReservationStatus(
                member, restaurant, ReservationStatus.VISITED)
                .orElseThrow(() -> new EntityNotFoundException("No reservation found"));

        if (reservation.getReservationDate().isBefore(LocalDate.now().minusDays(3))) {
            throw new IllegalArgumentException("You can only write a review within 3 days of reservation");
        }

        Review review = Review.builder()
                .content(reqDto.getContent())
                .rating(reqDto.getRating())
                .restaurant(restaurant)
                .member(member)
                .build();

        Review savedReview = reviewRepository.save(review);

        Set<ReviewImage> reviewImages = new HashSet<>();
        if (images != null && images.length > 0) {
            List<String> imageUrls = imageUploadService.uploadReviewImages(restaurantId, savedReview.getId(), images);

            for (String imageUrl: imageUrls) {
                ReviewImage reviewImage = new ReviewImage(imageUrl, savedReview);
                reviewImages.add(reviewImage);
            }
            reviewImageRepository.saveAll(reviewImages);
        }

        ReviewResDto resDto = new ReviewResDto(
                savedReview.getId(),
                restaurantId,
                "Review and rating added successfully"
        );

        return resDto;
    }

    // 식당 리뷰 목록 조회
    public List<ReviewInfoDto> getReviewsByRestaurant(Long restaurantId, Pageable pageable) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found with id: " + restaurantId));

        Page<Review> reviews = reviewRepository.findByRestaurant_Id(restaurantId, pageable);

        return reviews.stream()
                .map(this::convertToInfoDto)
                .collect(Collectors.toList());
    }

    // 리뷰 상세 조회
    public ReviewInfoDto getReviewById(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Review not found with id: " + id));

        return convertToInfoDto(review);
    }

    private ReviewInfoDto convertToInfoDto(Review review) {
        List<String> images = review.getImages().stream()
                .map(ReviewImage::getImageUrl)
                .collect(Collectors.toList());

        return ReviewInfoDto.builder()
                .id(review.getId())
                .content(review.getContent())
                .rating(review.getRating())
                .images(images)
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .restaurantId(review.getRestaurant().getId())
                .build();
    }

    // 리뷰 수정
    // TODO: 작성일 3일 이내에만 수정 가능하도록 하는 코드 추가
    public ReviewResDto updateReview(Long reviewId,
                                     Long restaurantId,
                                     ReviewUpdateReqDto reqDto,
                                     MultipartFile[] images) throws IOException {

        Review review = findRestaurantAndReview(reviewId, restaurantId);

        Review updatedReview = review.toBuilder()
                .content(reqDto.getContent() != null ? reqDto.getContent() : review.getContent())
                .rating(reqDto.getRating() != null ? reqDto.getRating() : review.getRating())
                .build();

        if (images != null && images.length > 0) {
            imageUploadService.deleteExistingReviewImages(review);

            Set<ReviewImage> newImages = new HashSet<>();

            List<String> imageUrls = imageUploadService.uploadReviewImages(restaurantId, reviewId, images);
            for (String imageUrl: imageUrls) {
                newImages.add(new ReviewImage(imageUrl, updatedReview));
            }

            updatedReview = updatedReview.toBuilder()
                    .images(newImages)
                    .build();
        }

        Review savedReview = reviewRepository.save(updatedReview);

        ReviewResDto resDto = new ReviewResDto(
                savedReview.getId(),
                restaurantId,
                "Review updated successfully"
        );
        return resDto;
    }

    // 리뷰 삭제
    public ResponseEntity<String> deleteReview(Long reviewId, Long restaurantId) {
        Review review = findRestaurantAndReview(reviewId, restaurantId);

        reviewRepository.delete(review);
        reviewImageRepository.deleteByReviewId(reviewId);

        return ResponseEntity.ok("Review deleted successfully");
    }

    // 레스토랑, 리뷰 검증
    private Review findRestaurantAndReview(Long reviewId, Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found with id: " + restaurantId));

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("Review not found with id: " + reviewId));

        if (!review.getRestaurant().getId().equals(restaurantId)) {
            throw new IllegalArgumentException("해당 레스토랑의 리뷰가 아닙니다");
        }

        return review;
    }
}
