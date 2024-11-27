package com.zero.bwtableback.restaurant.service;

import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.member.repository.MemberRepository;
import com.zero.bwtableback.restaurant.entity.Restaurant;
import com.zero.bwtableback.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RestaurantLikeService {

    private final RestaurantRepository restaurantRepository;
    private final MemberRepository memberRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public void likeRestaurant(Long memberId, Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        String key = "member:" + memberId + ":likes";
        redisTemplate.opsForSet().add(key, restaurantId);
    }

    public List<Long> getLikesByMember(Long memberId) {
        String key = "member:" + memberId + ":likes";
        Set<Object> likesSet = redisTemplate.opsForSet().members(key);

        if (likesSet != null) {
            return likesSet.stream()
                    .filter(obj -> obj instanceof Long)
                    .map(obj -> (Long)obj)
                    .collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }

    public void unlikeRestaurant(Long memberId, Long restaurantId) {
        String key = "member:" + memberId + ":likes";
        redisTemplate.opsForSet().remove(key, restaurantId);
    }
}
