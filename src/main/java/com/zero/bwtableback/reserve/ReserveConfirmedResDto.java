package com.zero.bwtableback.reserve;

import com.zero.bwtableback.member.entity.Member;
import com.zero.bwtableback.restaurant.entity.Restaurant;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.sound.midi.MetaMessage;

@Getter
@AllArgsConstructor
public class ReserveConfirmedResDto {
    Restaurant restaurant;
    Reserve reserve;
}
