package com.zero.bwtableback.restaurant.repository;

import com.zero.bwtableback.restaurant.entity.Hashtag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HashtagRepository extends JpaRepository<Hashtag, Long> {

    Optional<Hashtag> findByName(String name);
}
