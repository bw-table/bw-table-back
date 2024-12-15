package com.zero.bwtableback.restaurant.repository;

import com.zero.bwtableback.restaurant.entity.Category;
import com.zero.bwtableback.restaurant.entity.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByCategoryType(CategoryType type);

    @Query("SELECT c FROM Category c ORDER BY c.searchCount DESC")
    List<Category> findMostPopularCategory();
}
