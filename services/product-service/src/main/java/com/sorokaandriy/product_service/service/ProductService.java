package com.sorokaandriy.product_service.service;

import com.sorokaandriy.product_service.dto.ProductRequest;
import com.sorokaandriy.product_service.dto.ProductResponse;
import com.sorokaandriy.product_service.exception.CategoryNotFoundException;
import com.sorokaandriy.product_service.exception.NotEnoughStockException;
import com.sorokaandriy.product_service.exception.ProductNotFoundException;
import com.sorokaandriy.product_service.model.CategoryEntity;
import com.sorokaandriy.product_service.model.ProductEntity;
import com.sorokaandriy.product_service.repository.CategoryRepository;
import com.sorokaandriy.product_service.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository repository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper mapper;

    public ProductService(ProductRepository repository,
                          CategoryRepository categoryRepository,
                          ProductMapper mapper) {
        this.repository = repository;
        this.categoryRepository = categoryRepository;
        this.mapper = mapper;
    }

    public Page<ProductResponse> search(Long categoryId,
                                        String brand,
                                        BigDecimal minPrice,
                                        BigDecimal maxPrice,
                                        String search,
                                        int page,
                                        int size,
                                        String sortBy,
                                        String direction) {

        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        return repository.search(categoryId, brand, minPrice, maxPrice, search, pageable)
                .map(mapper::fromProductToResponse);
    }

    public ProductResponse findById(Long productId) {
        return mapper.fromProductToResponse(getProduct(productId));
    }

    public ProductResponse createProduct(ProductRequest request) {
        CategoryEntity category = getCategory(request.getCategoryId());
        ProductEntity product = mapper.fromRequestToProduct(request, category);
        return mapper.fromProductToResponse(repository.save(product));
    }

    public ProductResponse updateProduct(Long productId, ProductRequest request) {
        ProductEntity product = getProduct(productId);
        CategoryEntity category = getCategory(request.getCategoryId());
        return mapper.fromProductToResponse(repository.save(
                mapper.fromRequestToExistingProduct(product, request, category)));
    }

    public String deleteProduct(Long productId) {
        ProductEntity product = getProduct(productId);
        product.setActive(false);
        repository.save(product);
        return "Product with id " + productId + " was deactivated";
    }

    @Transactional
    public void decreaseStock(Long productId, int quantity) {
        ProductEntity product = getProduct(productId);

        if (product.getStockQuantity() < quantity) {
            throw new NotEnoughStockException("Product " + product.getName() +
                    " has only " + product.getStockQuantity() + " items left");
        }

        product.setStockQuantity(product.getStockQuantity() - quantity);
        repository.save(product);
        log.info("Stock for product {} decreased by {}", productId, quantity);
    }

    private ProductEntity getProduct(Long productId) {
        return repository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product with id " + productId + " not found"));
    }

    private CategoryEntity getCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Category with id " + categoryId + " not found"));
    }
}
