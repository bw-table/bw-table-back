package com.zero.bwtableback.restaurant.service;

import com.zero.bwtableback.restaurant.dto.RestaurantReqDto;
import com.zero.bwtableback.restaurant.entity.Restaurant;

public interface RestaurantService {

    Restaurant registerRestaurant(RestaurantReqDto restaurantDto);
}
