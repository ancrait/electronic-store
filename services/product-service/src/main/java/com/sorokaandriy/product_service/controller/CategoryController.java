package com.sorokaandriy.product_service.controller;

import com.sorokaandriy.product_service.dto.CategoryRequest;
import com.sorokaandriy.product_service.dto.CategoryResponse;
import com.sorokaandriy.product_service.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService service;

    public CategoryController(CategoryService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> findById(
            @PathVariable Long categoryId
    ) {
        return ResponseEntity.ok(service.findById(categoryId));
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CategoryRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createCategory(request));
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long categoryId,
            @Valid @RequestBody CategoryRequest request
    ) {
        return ResponseEntity.ok(service.updateCategory(categoryId, request));
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<String> deleteCategory(
            @PathVariable Long categoryId
    ) {
        return ResponseEntity.ok(service.deleteCategory(categoryId));
    }
}
