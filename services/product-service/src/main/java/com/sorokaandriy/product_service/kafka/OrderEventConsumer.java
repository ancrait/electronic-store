package com.sorokaandriy.product_service.kafka;

import com.sorokaandriy.product_service.exception.NotEnoughStockException;
import com.sorokaandriy.product_service.exception.ProductNotFoundException;
import com.sorokaandriy.product_service.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

    private final ProductService productService;

    public OrderEventConsumer(ProductService productService) {
        this.productService = productService;
    }

    @KafkaListener(
            topics = "${kafka.topic.order-created}",
            groupId = "product-service-group",
            containerFactory = "orderCreatedListenerFactory"
    )
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent for order {}", event.getOrderId());

        for (OrderItemEvent item : event.getItems()) {
            try {
                productService.decreaseStock(item.getProductId(), item.getQuantity());
            } catch (ProductNotFoundException | NotEnoughStockException exception) {
                log.warn("Cannot decrease stock for product {}: {}", item.getProductId(), exception.getMessage());
            }
        }
    }
}
