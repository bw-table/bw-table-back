package com.zero.bwtableback.restaurant.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.zero.bwtableback.common.BaseEntity;
import com.zero.bwtableback.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "restaurant")
public class Restaurant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private double latitude; // 위도

    @Column(nullable = false)
    private double longitude; // 경도

    @Column(nullable = false)
    private String contact;

    private String closedDay; // 정기휴무일(요일)

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL)
    @Column(nullable = false)
    private List<OperatingHours> operatingHours;

    private String info; // 안내 및 유의사항

    private String link; // 홈페이지 링크

    @Column(nullable = false)
    private int deposit; // 인당예약금

    @OneToMany(
            mappedBy = "restaurant", // 양방향 관계 설정
            cascade = CascadeType.ALL, // 음식점 삭제 시 관련 이미지 함께 삭제
            fetch = FetchType.LAZY // 이미지 필요할 때만 로드
    )
    @Column(nullable = false)
    private Set<RestaurantImage> images;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Column(nullable = false)
    private List<Menu> menus;

    @ManyToMany
    @JoinTable(
            name = "restaurant_facility", // 조인테이블 이름
            joinColumns = @JoinColumn(name = "restaurant_id"), // 레스토랑 외래키
            inverseJoinColumns = @JoinColumn(name = "facility_id") // 편의시설 외래키
    )
    private List<Facility> facilities;

    @ManyToMany
    @JoinTable(
            name = "restaurant_hashtag",
            joinColumns = @JoinColumn(name = "restaurant_id"),
            inverseJoinColumns = @JoinColumn(name = "hashtag_id")
    )
    private List<Hashtag> hashtags;

    @Column(nullable = false)
    private double averageRating; // 평균 평점

    @OneToOne
    @JoinColumn(name = "member_id", nullable = true)
    private Member member;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL)
    private List<Review> reviews;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Announcement> announcements;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReservationCapacity> reservationCapacities = new ArrayList<>();
}
