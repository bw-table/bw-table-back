package com.zero.bwtableback.restaurant.repository;

import com.zero.bwtableback.restaurant.entity.Category;
import com.zero.bwtableback.restaurant.entity.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByCategoryType(CategoryType type);

    @Query("select c from Category c order by c.searchCount desc")
    Category findMostpopularCategory();
}
