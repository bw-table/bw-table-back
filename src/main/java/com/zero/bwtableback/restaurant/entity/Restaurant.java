package com.zero.bwtableback.restaurant.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Set;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "restaurant")
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String contact;

    private String closedDay; // 휴무일

    @OneToMany(mappedBy = "restaurant")
    private List<OperatingHours> operatingHours;

    @OneToMany(
            mappedBy = "restaurant", // 양방향 관계 설정
            cascade = CascadeType.ALL, // 음식점 삭제 시 관련 이미지 함께 삭제
            fetch = FetchType.LAZY // 이미지 필요할 때만 로드
    )
    private Set<RestaurantImage> images;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "restaurant")
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

//    private String category;
//    private String closed_days;
//    private String hashtag;
}
