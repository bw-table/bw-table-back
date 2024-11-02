package com.zero.bwtableback.restaurant.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "hashtag")
public class Hashtag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private int searchCount;

    @ManyToMany(mappedBy = "hashtags")
    private List<Restaurant> restaurants;

    public Hashtag(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Hashtag(String name) {
        this.name = name;
    }
}
