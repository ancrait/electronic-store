package com.sorokaandriy.product_service.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreatedEvent {

    private Long orderId;
    private String userId;
    private String email;
    private String customerName;
    private String deliveryAddress;
    private BigDecimal totalPrice;
    private List<OrderItemEvent> items;
    private Long createdAt;
}
