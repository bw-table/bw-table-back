package com.zero.bwtableback.restaurant.config;

import com.zero.bwtableback.restaurant.entity.Category;
import com.zero.bwtableback.restaurant.entity.CategoryType;
import com.zero.bwtableback.restaurant.entity.Facility;
import com.zero.bwtableback.restaurant.entity.FacilityType;
import com.zero.bwtableback.restaurant.repository.CategoryRepository;
import com.zero.bwtableback.restaurant.repository.FacilityRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final FacilityRepository facilityRepository;

    public DatabaseInitializer(CategoryRepository categoryRepository, FacilityRepository facilityRepository) {
        this.categoryRepository = categoryRepository;
        this.facilityRepository = facilityRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        initializeCategories();
        initializeFacilities();
    }

    private void initializeCategories() {
        if (categoryRepository.count() == 0) {
            Arrays.stream(CategoryType.values())
                    .forEach(type -> {
                        Category category = Category.builder()
                                .categoryType(type)
                                .searchCount(0)
                                .build();
                        categoryRepository.save(category);
                    });
        }
    }

    private void initializeFacilities() {
        if (facilityRepository.count() == 0) {
            Arrays.stream(FacilityType.values())
                    .forEach(type -> {
                        Facility facility = Facility.builder()
                                .facilityType(type)
                                .build();
                        facilityRepository.save(facility);
                    });
        }
    }
}