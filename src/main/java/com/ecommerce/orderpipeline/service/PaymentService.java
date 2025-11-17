package com.ecommerce.orderpipeline.service;

import com.ecommerce.orderpipeline.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PaymentService {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Value("${app.payment.timeout:5000}")
    private int paymentTimeout;
    
    @Value("${app.payment.max-retries:3}")
    private int maxRetries;
    
    @Retryable(value = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public boolean processPayment(Order order) {
        logger.info("Processing payment for order: {}, amount: {}", 
            order.getOrderId(), order.getTotalAmount());
        
        // Simulate payment processing with external gateway
        try {
            // In a real scenario, this would call an actual payment gateway
            boolean paymentSuccess = simulatePaymentGateway(order);
            
            if (!paymentSuccess) {
                throw new RuntimeException("Payment gateway declined the transaction");
            }
            
            logger.info("Payment processed successfully for order: {}", order.getOrderId());
            return true;
            
        } catch (Exception e) {
            logger.error("Payment processing failed for order: {}", order.getOrderId(), e);
            throw new RuntimeException("Payment processing failed: " + e.getMessage());
        }
    }
    
    private boolean simulatePaymentGateway(Order order) {
        // Simulate payment gateway response
        // 95% success rate for demo purposes
        return Math.random() > 0.05;
    }
}
