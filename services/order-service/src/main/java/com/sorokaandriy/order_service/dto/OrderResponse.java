package com.sorokaandriy.order_service.dto;

import com.sorokaandriy.order_service.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderResponse {

    private Long id;
    private String userId;
    private String email;
    private String customerName;
    private String phone;
    private String deliveryAddress;
    private String comment;
    private BigDecimal totalPrice;
    private OrderStatus status;
    private List<OrderItemResponse> items;
    private Long createdAt;
}
