package com.zero.bwtableback.restaurant.repository;

import com.zero.bwtableback.restaurant.entity.Category;
import com.zero.bwtableback.restaurant.entity.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByCategoryType(CategoryType type);
}
