package com.zero.bwtableback.restaurant.controller;

import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.restaurant.exception.RestaurantException;
import com.zero.bwtableback.restaurant.service.RestaurantLikeService;
import com.zero.bwtableback.security.MemberDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantLikeController {

    private final RestaurantLikeService restaurantLikeService;

    @PostMapping("/{restaurantId}/likes")
    public ResponseEntity<String> likeRestaurant(@PathVariable Long restaurantId,
                                                 @AuthenticationPrincipal MemberDetails memberDetails) {

        Member member = memberDetails.getMember();

        try {
            restaurantLikeService.likeRestaurant(member.getId(), restaurantId);
            return ResponseEntity.ok("Restaurant liked successfully");
        } catch (RestaurantException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Restaurant not found");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Server error occurred");
        }
    }

    @GetMapping("/likes")
    public ResponseEntity<List<Long>> getLikesByMember(@AuthenticationPrincipal MemberDetails memberDetails) {
        Member member = memberDetails.getMember();

        List<Long> likedRestaurantIds = restaurantLikeService.getLikesByMember(member.getId());
        return ResponseEntity.ok(likedRestaurantIds);
    }

    @DeleteMapping("/{restaurantId}/unlikes")
    public ResponseEntity<String> unlikeRestaurant(@PathVariable Long restaurantId,
                                                   @AuthenticationPrincipal MemberDetails memberDetails) {

        Member member = memberDetails.getMember();
        try {
            restaurantLikeService.unlikeRestaurant(member.getId(), restaurantId);
            return ResponseEntity.ok("Restaurant unliked successfully");
        } catch (RestaurantException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Restaurant not found");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Server error occurred");
        }
    }
}
