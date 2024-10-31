package com.zero.bwtableback.restaurant.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "facility")
public class Facility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private FacilityType facilityType;

    @ManyToMany(mappedBy = "facilities")
    private List<Restaurant> restaurants;

    public Facility(Long id, FacilityType type) {
        this.id = id;
        this.facilityType = type;
    }
}
