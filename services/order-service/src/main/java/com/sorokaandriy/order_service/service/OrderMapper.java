package com.sorokaandriy.order_service.service;

import com.sorokaandriy.order_service.dto.OrderItemResponse;
import com.sorokaandriy.order_service.dto.OrderResponse;
import com.sorokaandriy.order_service.kafka.OrderCreatedEvent;
import com.sorokaandriy.order_service.kafka.OrderItemEvent;
import com.sorokaandriy.order_service.model.OrderEntity;
import com.sorokaandriy.order_service.model.OrderItemEntity;
import org.springframework.stereotype.Service;

@Service
public class OrderMapper {

    public OrderResponse fromOrderToResponse(OrderEntity order) {
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .email(order.getEmail())
                .customerName(order.getCustomerName())
                .phone(order.getPhone())
                .deliveryAddress(order.getDeliveryAddress())
                .comment(order.getComment())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .items(order.getItems().stream().map(this::fromItemToResponse).toList())
                .createdAt(order.getCreatedAt())
                .build();
    }

    public OrderItemResponse fromItemToResponse(OrderItemEntity item) {
        return OrderItemResponse.builder()
                .productId(item.getProductId())
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .subtotal(item.getSubtotal())
                .build();
    }

    public OrderCreatedEvent fromOrderToEvent(OrderEntity order) {
        return new OrderCreatedEvent(
                order.getId(),
                order.getUserId(),
                order.getEmail(),
                order.getCustomerName(),
                order.getDeliveryAddress(),
                order.getTotalPrice(),
                order.getItems().stream()
                        .map(item -> new OrderItemEvent(
                                item.getProductId(),
                                item.getProductName(),
                                item.getQuantity(),
                                item.getPrice()))
                        .toList(),
                order.getCreatedAt()
        );
    }
}
