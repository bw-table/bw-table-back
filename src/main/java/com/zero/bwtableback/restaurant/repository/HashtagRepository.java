package com.zero.bwtableback.restaurant.repository;

import com.zero.bwtableback.restaurant.entity.Hashtag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HashtagRepository extends JpaRepository<Hashtag, Long> {

    Optional<Hashtag> findByName(String name);
    List<Hashtag> findTop10ByNameStartingWithIgnoreCase(String hashtag);
}
