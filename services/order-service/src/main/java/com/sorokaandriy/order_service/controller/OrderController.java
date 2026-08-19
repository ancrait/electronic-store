package com.sorokaandriy.order_service.controller;

import com.sorokaandriy.order_service.dto.CreateOrderRequest;
import com.sorokaandriy.order_service.dto.OrderResponse;
import com.sorokaandriy.order_service.dto.UpdateOrderStatusRequest;
import com.sorokaandriy.order_service.security.AuthenticatedUser;
import com.sorokaandriy.order_service.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CreateOrderRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createOrder(user, request));
    }

    @GetMapping("/my")
    public ResponseEntity<Page<OrderResponse>> findMyOrders(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(service.findMyOrders(user.id(), page, size));
    }

    @GetMapping("/all")
    public ResponseEntity<Page<OrderResponse>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(service.findAll(page, size));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> findById(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long orderId
    ) {
        return ResponseEntity.ok(service.findById(user, orderId));
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request
    ) {
        return ResponseEntity.ok(service.updateStatus(orderId, request.getStatus()));
    }
}
