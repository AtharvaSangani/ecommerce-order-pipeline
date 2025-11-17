package com.ecommerce.orderpipeline.dto;

import com.ecommerce.orderpipeline.model.Order;
import com.ecommerce.orderpipeline.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {
    private String eventId;
    private String orderId;
    private Order order;
    private OrderStatus status;
    private String message;
    private LocalDateTime timestamp;
    private String sourceService;
    
    public OrderEvent(String orderId, Order order, OrderStatus status, String message, String sourceService) {
        this.eventId = java.util.UUID.randomUUID().toString();
        this.orderId = orderId;
        this.order = order;
        this.status = status;
        this.message = message;
        this.sourceService = sourceService;
        this.timestamp = LocalDateTime.now();
    }
}