package com.sorokaandriy.notification_service.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemEvent {

    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal price;
}
