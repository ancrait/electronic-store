package com.sorokaandriy.product_service.service;

import com.sorokaandriy.product_service.dto.CategoryRequest;
import com.sorokaandriy.product_service.dto.CategoryResponse;
import com.sorokaandriy.product_service.exception.CategoryNotFoundException;
import com.sorokaandriy.product_service.model.CategoryEntity;
import com.sorokaandriy.product_service.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository repository;
    private final ProductMapper mapper;

    public CategoryService(CategoryRepository repository, ProductMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<CategoryResponse> findAll() {
        return repository.findAll().stream()
                .map(mapper::fromCategoryToResponse)
                .toList();
    }

    public CategoryResponse findById(Long categoryId) {
        return mapper.fromCategoryToResponse(getCategory(categoryId));
    }

    public CategoryResponse createCategory(CategoryRequest request) {
        return mapper.fromCategoryToResponse(repository.save(mapper.fromRequestToCategory(request)));
    }

    public CategoryResponse updateCategory(Long categoryId, CategoryRequest request) {
        CategoryEntity category = getCategory(categoryId);
        category.setName(request.getName());
        category.setSlug(request.getSlug());
        category.setDescription(request.getDescription());
        return mapper.fromCategoryToResponse(repository.save(category));
    }

    public String deleteCategory(Long categoryId) {
        repository.delete(getCategory(categoryId));
        return "Category with id " + categoryId + " was successfully deleted";
    }

    private CategoryEntity getCategory(Long categoryId) {
        return repository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Category with id " + categoryId + " not found"));
    }
}
