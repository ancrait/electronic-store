package com.sorokaandriy.product_service.repository;

import com.sorokaandriy.product_service.model.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {

    Optional<CategoryEntity> findBySlug(String slug);

    boolean existsByName(String name);
}
