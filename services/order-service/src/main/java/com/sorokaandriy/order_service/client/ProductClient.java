package com.sorokaandriy.order_service.client;

import com.sorokaandriy.order_service.exception.ProductUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class ProductClient {

    private static final Logger log = LoggerFactory.getLogger(ProductClient.class);

    private final RestClient restClient;

    public ProductClient(RestClient productRestClient) {
        this.restClient = productRestClient;
    }

    public ProductClientResponse findProductById(Long productId) {
        try {
            ProductClientResponse product = restClient.get()
                    .uri("/api/products/{productId}", productId)
                    .retrieve()
                    .body(ProductClientResponse.class);

            if (product == null) {
                throw new ProductUnavailableException("Product with id " + productId + " not found");
            }

            return product;
        } catch (RestClientException exception) {
            log.error("Cannot fetch product {}: {}", productId, exception.getMessage());
            throw new ProductUnavailableException("Product with id " + productId + " is currently unavailable");
        }
    }
}
