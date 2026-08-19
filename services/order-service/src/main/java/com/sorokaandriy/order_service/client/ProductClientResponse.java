package com.sorokaandriy.order_service.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductClientResponse {

    private Long id;
    private String name;
    private String brand;
    private BigDecimal price;
    private Integer stockQuantity;
    private boolean active;
}
