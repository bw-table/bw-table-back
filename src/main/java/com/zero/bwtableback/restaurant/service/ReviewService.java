package com.zero.bwtableback.restaurant.service;

import com.zero.bwtableback.common.service.ImageUploadService;
import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.member.repository.MemberRepository;
import com.zero.bwtableback.restaurant.dto.ReviewDetailDto;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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

    // 리뷰 작성
    public ReviewResDto createReview(Long restaurantId,
                                     ReviewReqDto reqDto,
                                     MultipartFile[] images,
                                     Member member) throws IOException {

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found with id: " + restaurantId));

//        Reservation reservation = reservationRepository.findByMemberAndRestaurantAndReservationDateBetween(
//                member, restaurant, LocalDate.now().minusDays(3), LocalDate.now())
//                .orElseThrow(() -> new EntityNotFoundException("No reservation found within the last 3 days"));
//
//        if (reservation.getReservationStatus() != ReservationStatus.VISITED) {
//            throw new IllegalArgumentException("You can write review only when you visited");
//        }

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

        // restaurant 평균 평점 update
        updateRestaurantAverageRating(restaurant);

        ReviewResDto resDto = new ReviewResDto(
                savedReview.getId(),
                restaurantId,
                "Review and rating added successfully"
        );

        return resDto;
    }

    // 식당 리뷰 목록 조회
    public List<ReviewDetailDto> getReviewsByRestaurant(Long restaurantId, Pageable pageable) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found with id: " + restaurantId));

        Page<Review> reviews = reviewRepository.findByRestaurant_Id(restaurantId, pageable);

        return reviews.stream()
                .map(this::convertToDetailDto)
                .collect(Collectors.toList());
    }

    // 리뷰 상세 조회 (필요없음)
    public ReviewDetailDto getReviewById(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Review not found with id: " + id));

        return convertToDetailDto(review);
    }

    private ReviewDetailDto convertToDetailDto(Review review) {
        List<String> images = review.getImages().stream()
                .map(ReviewImage::getImageUrl)
                .collect(Collectors.toList());

        return ReviewDetailDto.builder()
                .id(review.getId())
                .content(review.getContent())
                .rating(review.getRating())
                .images(images)
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .restaurantId(review.getRestaurant().getId())
                .memberId(review.getMember().getId())
                .memberProfileImage(review.getMember().getProfileImage())
                .memberNickname(review.getMember().getNickname())
                .build();
    }

    // 리뷰 수정
    public ReviewResDto updateReview(Long reviewId,
                                     Long restaurantId,
                                     ReviewUpdateReqDto reqDto,
                                     MultipartFile[] images,
                                     Member member) throws IOException {

        Review review = findRestaurantAndReview(reviewId, restaurantId);

        if (!review.getMember().getId().equals(member.getId())) {
            throw new AccessDeniedException("You can only update your own reviews");
        }

        // 리뷰 작성일 기준 3일 이내에만 수정 가능
        LocalDate reviewDate = review.getCreatedAt().toLocalDate();

        long daysBetween = ChronoUnit.DAYS.between(reviewDate, LocalDate.now());
        if (daysBetween > 3) {
            throw new IllegalArgumentException("You can only update reviews within 3 days of creation");
        }

        Review updatedReview = review.toBuilder()
                .content(reqDto.getContent() != null ? reqDto.getContent() : review.getContent())
                .rating(reqDto.getRating() != null ? reqDto.getRating() : review.getRating())
                .build();

        // 삭제할 이미지가 있는 경우 기존 이미지 삭제
        if (reqDto.getImageIdsToDelete() != null && !reqDto.getImageIdsToDelete().isEmpty()) {
            for (Long imageId: reqDto.getImageIdsToDelete()) {
                imageUploadService.deleteReviewImageFile(imageId);
            }
        }

        // 새로운 이미지를 추가할 경우
        Set<ReviewImage> newImages = new HashSet<>();
        if (images != null && images.length > 0) {
            List<String> imageUrls = imageUploadService.uploadReviewImages(restaurantId, reviewId, images);

            Set<ReviewImage> existingImages = review.getImages();
            int currentImageCount = existingImages.size();
            int availableSpace = 5 - currentImageCount;

            for (String imageUrl: imageUrls) {
                if (availableSpace <= 0) {
                    break;
                }

                newImages.add(new ReviewImage(imageUrl, updatedReview));
                availableSpace--;
            }
        }

        // 기존 이미지와 새 이미지 리뷰에 반영
        Set<ReviewImage> finalImages = new HashSet<>(review.getImages());
        finalImages.addAll(newImages);

        updatedReview = updatedReview.toBuilder()
                .images(newImages)
                .build();

        Review savedReview = reviewRepository.save(updatedReview);

        ReviewResDto resDto = new ReviewResDto(
                savedReview.getId(),
                restaurantId,
                "Review updated successfully"
        );
        return resDto;
    }

    // 리뷰 삭제
    public ResponseEntity<String> deleteReview(Long reviewId, Long restaurantId, Member member) throws AccessDeniedException {
        Review review = findRestaurantAndReview(reviewId, restaurantId);

        if (!review.getMember().getId().equals(member.getId())) {
            if (!isRestaurantOwner(member, restaurantId)) {
                throw new AccessDeniedException("Reviews only can be deleted by owners and writers");
            }
        }

        reviewRepository.delete(review);
        reviewImageRepository.deleteByReviewId(reviewId);

        return ResponseEntity.ok("Review deleted successfully");
    }

    // 특정 식당의 사장님인지 확인
    private boolean isRestaurantOwner(Member member, Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found"));

        return restaurant.getMember().equals(member);
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

    // 평균 평점 업데이트
    private void updateRestaurantAverageRating(Restaurant restaurant) {
        List<Review> reviews = reviewRepository.findByRestaurant(restaurant);

        if (reviews.isEmpty()) {
            restaurant.setAverageRating(0);
        } else {
            double average = reviews.stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0);

            BigDecimal roundedAverage = new BigDecimal(average).setScale(1, RoundingMode.HALF_UP);

            restaurant.setAverageRating(roundedAverage.doubleValue());
        }

        restaurantRepository.save(restaurant);
    }
}
