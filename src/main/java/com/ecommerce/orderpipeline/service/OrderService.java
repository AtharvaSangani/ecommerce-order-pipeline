package com.ecommerce.orderpipeline.service;

import com.ecommerce.orderpipeline.model.Order;
import com.ecommerce.orderpipeline.model.OrderStatus;
import com.ecommerce.orderpipeline.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Transactional
    @Retryable(value = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public Order validateOrder(Order order) {
        logger.info("Validating order: {}", order.getOrderId());
        
        // Simulate validation logic
        if (order.getCustomerId() == null || order.getCustomerId().isEmpty()) {
            throw new RuntimeException("Invalid customer ID");
        }
        
        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new RuntimeException("Order must contain items");
        }
        
        if (order.getTotalAmount() == null || order.getTotalAmount() <= 0) {
            throw new RuntimeException("Invalid order total");
        }
        
        order.setStatus(OrderStatus.VALIDATED);
        Order savedOrder = orderRepository.save(order);
        logger.info("Order validation successful: {}", order.getOrderId());
        
        return savedOrder;
    }
    
    @Transactional
    public Order confirmOrder(Order order) {
        logger.info("Confirming order: {}", order.getOrderId());
        
        order.setStatus(OrderStatus.CONFIRMED);
        Order confirmedOrder = orderRepository.save(order);
        logger.info("Order confirmed successfully: {}", order.getOrderId());
        
        return confirmedOrder;
    }
    
    public Order getOrder(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
    }
    
    @Transactional
    public Order saveOrder(Order order) {
        return orderRepository.save(order);
    }
}