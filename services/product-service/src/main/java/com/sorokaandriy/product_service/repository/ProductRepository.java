package com.sorokaandriy.product_service.repository;

import com.sorokaandriy.product_service.model.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    @Query("""
            SELECT p FROM ProductEntity p
            WHERE p.active = true
              AND (:categoryId IS NULL OR p.category.id = :categoryId)
              AND (CAST(:brand AS String) IS NULL OR LOWER(p.brand) = LOWER(CAST(:brand AS String)))
              AND (:minPrice IS NULL OR p.price >= :minPrice)
              AND (:maxPrice IS NULL OR p.price <= :maxPrice)
              AND (CAST(:search AS String) IS NULL
                   OR LOWER(p.name) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%'))
                   OR LOWER(p.brand) LIKE LOWER(CONCAT('%', CAST(:search AS String), '%')))
            """)
    Page<ProductEntity> search(@Param("categoryId") Long categoryId,
                               @Param("brand") String brand,
                               @Param("minPrice") BigDecimal minPrice,
                               @Param("maxPrice") BigDecimal maxPrice,
                               @Param("search") String search,
                               Pageable pageable);
}
