package com.zero.bwtableback.reserve;

import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.restaurant.entity.Restaurant;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReserveCreateResDto {
    private Member member;
    private Restaurant restaurant;
}

