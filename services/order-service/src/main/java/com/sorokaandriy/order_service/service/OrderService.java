package com.sorokaandriy.order_service.service;

import com.sorokaandriy.order_service.client.ProductClient;
import com.sorokaandriy.order_service.client.ProductClientResponse;
import com.sorokaandriy.order_service.dto.CreateOrderRequest;
import com.sorokaandriy.order_service.dto.OrderItemRequest;
import com.sorokaandriy.order_service.dto.OrderResponse;
import com.sorokaandriy.order_service.exception.AccessDeniedForOrderException;
import com.sorokaandriy.order_service.exception.OrderNotFoundException;
import com.sorokaandriy.order_service.exception.ProductUnavailableException;
import com.sorokaandriy.order_service.kafka.OrderEventProducer;
import com.sorokaandriy.order_service.model.OrderEntity;
import com.sorokaandriy.order_service.model.OrderItemEntity;
import com.sorokaandriy.order_service.model.OrderStatus;
import com.sorokaandriy.order_service.repository.OrderRepository;
import com.sorokaandriy.order_service.security.AuthenticatedUser;
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
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository repository;
    private final OrderMapper mapper;
    private final ProductClient productClient;
    private final OrderEventProducer eventProducer;

    public OrderService(OrderRepository repository,
                        OrderMapper mapper,
                        ProductClient productClient,
                        OrderEventProducer eventProducer) {
        this.repository = repository;
        this.mapper = mapper;
        this.productClient = productClient;
        this.eventProducer = eventProducer;
    }

    @Transactional
    public OrderResponse createOrder(AuthenticatedUser user, CreateOrderRequest request) {
        OrderEntity order = OrderEntity.builder()
                .userId(user.id())
                .email(user.email())
                .customerName(request.getCustomerName())
                .phone(request.getPhone())
                .deliveryAddress(request.getDeliveryAddress())
                .comment(request.getComment())
                .status(OrderStatus.NEW)
                .totalPrice(BigDecimal.ZERO)
                .build();

        BigDecimal totalPrice = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.getItems()) {
            ProductClientResponse product = productClient.findProductById(itemRequest.getProductId());

            if (!product.isActive()) {
                throw new ProductUnavailableException("Product " + product.getName() + " is no longer available");
            }

            if (product.getStockQuantity() < itemRequest.getQuantity()) {
                throw new ProductUnavailableException("Product " + product.getName() +
                        " has only " + product.getStockQuantity() + " items left");
            }

            OrderItemEntity item = OrderItemEntity.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .quantity(itemRequest.getQuantity())
                    .price(product.getPrice())
                    .build();

            order.addItem(item);
            totalPrice = totalPrice.add(item.getSubtotal());
        }

        order.setTotalPrice(totalPrice);
        OrderEntity saved = repository.save(order);

        eventProducer.sendOrderCreatedEvent(mapper.fromOrderToEvent(saved));
        log.info("Order {} created by user {}", saved.getId(), user.id());

        return mapper.fromOrderToResponse(saved);
    }

    public Page<OrderResponse> findMyOrders(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return repository.findAllByUserId(userId, pageable).map(mapper::fromOrderToResponse);
    }

    public Page<OrderResponse> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return repository.findAll(pageable).map(mapper::fromOrderToResponse);
    }

    public OrderResponse findById(AuthenticatedUser user, Long orderId) {
        OrderEntity order = getOrder(orderId);

        if (!order.getUserId().equals(user.id()) && !"ADMIN".equals(user.role())) {
            throw new AccessDeniedForOrderException("Order " + orderId + " belongs to another user");
        }

        return mapper.fromOrderToResponse(order);
    }

    public OrderResponse updateStatus(Long orderId, OrderStatus status) {
        OrderEntity order = getOrder(orderId);
        order.setStatus(status);
        return mapper.fromOrderToResponse(repository.save(order));
    }

    private OrderEntity getOrder(Long orderId) {
        return repository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order with id " + orderId + " not found"));
    }
}
