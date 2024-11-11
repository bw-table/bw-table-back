package com.zero.bwtableback.restaurant.entity;

import com.zero.bwtableback.common.BaseEntity;
import com.zero.bwtableback.restaurant.entity.Restaurant;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "review")
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private int rating;

    @OneToMany(
            mappedBy = "review",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY
    )
    private Set<ReviewImage> images;

    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;
}
