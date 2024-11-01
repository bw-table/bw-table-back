package com.zero.bwtableback.restaurant.service;

import com.zero.bwtableback.restaurant.dto.RestaurantListDto;
import com.zero.bwtableback.restaurant.dto.RegisterReqDto;
import com.zero.bwtableback.restaurant.entity.Restaurant;

import java.util.List;

public interface RestaurantService {

    Restaurant registerRestaurant(RegisterReqDto restaurantDto);

    List<RestaurantListDto> getRestaurants();


}
