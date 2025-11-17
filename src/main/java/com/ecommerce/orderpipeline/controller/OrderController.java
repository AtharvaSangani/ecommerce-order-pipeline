package com.ecommerce.orderpipeline.controller;

import com.ecommerce.orderpipeline.dto.OrderEvent;
import com.ecommerce.orderpipeline.model.Order;
import com.ecommerce.orderpipeline.model.OrderStatus;
import com.ecommerce.orderpipeline.service.OrderService;
import com.ecommerce.orderpipeline.kafka.producer.OrderEventProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private OrderEventProducer eventProducer;
    
    @PostMapping
    public ResponseEntity<String> placeOrder(@Valid @RequestBody Order order) {
        try {
            // Generate order ID if not provided
            if (order.getOrderId() == null) {
                order.setOrderId(UUID.randomUUID().toString());
            }
            
            order.setStatus(OrderStatus.PLACED);
            Order savedOrder = orderService.saveOrder(order);
            
            // Create and send order placed event
            OrderEvent orderEvent = new OrderEvent(
                savedOrder.getOrderId(),
                savedOrder,
                OrderStatus.PLACED,
                "Order placed successfully",
                "OrderAPI"
            );
            
            eventProducer.sendOrderPlacedEvent(orderEvent);
            
            return ResponseEntity.ok(
                String.format("Order placed successfully. Order ID: %s", savedOrder.getOrderId()));
                
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Failed to place order: " + e.getMessage());
        }
    }
    
    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrder(@PathVariable String orderId) {
        try {
            Order order = orderService.getOrder(orderId);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Order Processing Service is healthy");
    }
}