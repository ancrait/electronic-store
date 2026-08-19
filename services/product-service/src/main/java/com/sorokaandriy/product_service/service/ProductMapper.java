package com.sorokaandriy.product_service.service;

import com.sorokaandriy.product_service.dto.CategoryRequest;
import com.sorokaandriy.product_service.dto.CategoryResponse;
import com.sorokaandriy.product_service.dto.ProductRequest;
import com.sorokaandriy.product_service.dto.ProductResponse;
import com.sorokaandriy.product_service.model.CategoryEntity;
import com.sorokaandriy.product_service.model.ProductEntity;
import org.springframework.stereotype.Service;

@Service
public class ProductMapper {

    public ProductEntity fromRequestToProduct(ProductRequest request, CategoryEntity category) {
        return ProductEntity.builder()
                .name(request.getName())
                .brand(request.getBrand())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .imageUrl(request.getImageUrl())
                .category(category)
                .active(true)
                .build();
    }

    public ProductEntity fromRequestToExistingProduct(ProductEntity product, ProductRequest request, CategoryEntity category) {
        product.setName(request.getName());
        product.setBrand(request.getBrand());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setImageUrl(request.getImageUrl());
        product.setCategory(category);
        return product;
    }

    public ProductResponse fromProductToResponse(ProductEntity product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .brand(product.getBrand())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .imageUrl(product.getImageUrl())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .active(product.isActive())
                .createdAt(product.getCreatedAt())
                .build();
    }

    public CategoryEntity fromRequestToCategory(CategoryRequest request) {
        return CategoryEntity.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .description(request.getDescription())
                .build();
    }

    public CategoryResponse fromCategoryToResponse(CategoryEntity category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .build();
    }
}
