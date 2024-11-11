package com.zero.bwtableback.restaurant.repository;

import com.zero.bwtableback.restaurant.entity.CategoryType;
import com.zero.bwtableback.restaurant.entity.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    boolean existsByAddress(String address);
    boolean existsByContact(String contact);

    Page<Restaurant> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Restaurant> findByCategory_CategoryType(CategoryType type, Pageable pageable);
    Page<Restaurant> findByHashtags_NameContaining(String hashtag, Pageable pageable);
    Page<Restaurant> findByMenus_NameContaining(String menu, Pageable pageable);

    // 사장님(OWNER)의 회원 아이디로 레스토랑 아이디 조회
    @Query("SELECT r.id FROM Restaurant r WHERE r.member.id = :memberId")
    Long findRestaurantIdByMemberId(@Param("memberId") Long memberId);
}
