package com.ecommerce.orderpipeline.kafka.consumer;

import com.ecommerce.orderpipeline.dto.OrderEvent;
import com.ecommerce.orderpipeline.model.Order;
import com.ecommerce.orderpipeline.model.OrderStatus;
import com.ecommerce.orderpipeline.service.InventoryService;
import com.ecommerce.orderpipeline.service.OrderService;
import com.ecommerce.orderpipeline.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Component
public class OrderEventConsumer {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderEventConsumer.class);
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private InventoryService inventoryService;
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private OrderEventProducer eventProducer;
    
    @KafkaListener(topics = "${app.kafka.topics.order-placed}")
    @Retryable(value = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void consumeOrderPlaced(@Payload OrderEvent event, Acknowledgment ack) {
        try {
            logger.info("Received OrderPlaced event for order: {}", event.getOrderId());
            
            // Validate order
            Order validatedOrder = orderService.validateOrder(event.getOrder());
            if (validatedOrder != null) {
                OrderEvent validatedEvent = new OrderEvent(
                    event.getOrderId(), 
                    validatedOrder, 
                    OrderStatus.VALIDATED, 
                    "Order validation successful", 
                    "OrderValidator"
                );
                eventProducer.sendOrderValidatedEvent(validatedEvent);
            } else {
                throw new RuntimeException("Order validation failed");
            }
            
            ack.acknowledge();
        } catch (Exception e) {
            logger.error("Error processing OrderPlaced event for order: {}", event.getOrderId(), e);
            handleFailure(event, e.getMessage(), ack);
        }
    }
    
    @KafkaListener(topics = "${app.kafka.topics.order-validated}")
    @Retryable(value = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void consumeOrderValidated(@Payload OrderEvent event, Acknowledgment ack) {
        try {
            logger.info("Processing inventory reservation for order: {}", event.getOrderId());
            
            boolean inventoryReserved = inventoryService.reserveInventory(event.getOrder());
            if (inventoryReserved) {
                OrderEvent reservedEvent = new OrderEvent(
                    event.getOrderId(), 
                    event.getOrder(), 
                    OrderStatus.INVENTORY_RESERVED, 
                    "Inventory reserved successfully", 
                    "InventoryManager"
                );
                eventProducer.sendInventoryReservedEvent(reservedEvent);
            } else {
                throw new RuntimeException("Inventory reservation failed");
            }
            
            ack.acknowledge();
        } catch (Exception e) {
            logger.error("Error processing OrderValidated event for order: {}", event.getOrderId(), e);
            handleFailure(event, e.getMessage(), ack);
        }
    }
    
    @KafkaListener(topics = "${app.kafka.topics.inventory-reserved}")
    @Retryable(value = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void consumeInventoryReserved(@Payload OrderEvent event, Acknowledgment ack) {
        try {
            logger.info("Processing payment for order: {}", event.getOrderId());
            
            boolean paymentProcessed = paymentService.processPayment(event.getOrder());
            if (paymentProcessed) {
                OrderEvent paymentEvent = new OrderEvent(
                    event.getOrderId(), 
                    event.getOrder(), 
                    OrderStatus.PAYMENT_PROCESSED, 
                    "Payment processed successfully", 
                    "PaymentProcessor"
                );
                eventProducer.sendPaymentProcessedEvent(paymentEvent);
            } else {
                throw new RuntimeException("Payment processing failed");
            }
            
            ack.acknowledge();
        } catch (Exception e) {
            logger.error("Error processing InventoryReserved event for order: {}", event.getOrderId(), e);
            handleFailure(event, e.getMessage(), ack);
            // Release reserved inventory on payment failure
            inventoryService.releaseInventory(event.getOrder());
        }
    }
    
    @KafkaListener(topics = "${app.kafka.topics.payment-processed}")
    public void consumePaymentProcessed(@Payload OrderEvent event, Acknowledgment ack) {
        try {
            logger.info("Finalizing order: {}", event.getOrderId());
            
            Order confirmedOrder = orderService.confirmOrder(event.getOrder());
            OrderEvent confirmedEvent = new OrderEvent(
                event.getOrderId(), 
                confirmedOrder, 
                OrderStatus.CONFIRMED, 
                "Order confirmed successfully", 
                "OrderCoordinator"
            );
            eventProducer.sendOrderConfirmedEvent(confirmedEvent);
            
            ack.acknowledge();
        } catch (Exception e) {
            logger.error("Error processing PaymentProcessed event for order: {}", event.getOrderId(), e);
            handleFailure(event, e.getMessage(), ack);
        }
    }
    
    private void handleFailure(OrderEvent event, String errorMessage, Acknowledgment ack) {
        try {
            event.getOrder().setFailureReason(errorMessage);
            event.getOrder().setRetryCount(event.getOrder().getRetryCount() + 1);
            
            OrderEvent failedEvent = new OrderEvent(
                event.getOrderId(), 
                event.getOrder(), 
                OrderStatus.CANCELLED, 
                "Order processing failed: " + errorMessage, 
                "ErrorHandler"
            );
            
            if (event.getOrder().getRetryCount() >= 3) {
                eventProducer.sendToDLQ(failedEvent);
                logger.warn("Order {} moved to DLQ after {} retries", 
                    event.getOrderId(), event.getOrder().getRetryCount());
            } else {
                eventProducer.sendOrderFailedEvent(failedEvent);
            }
            
            ack.acknowledge();
        } catch (Exception e) {
            logger.error("Error handling failure for order: {}", event.getOrderId(), e);
        }
    }
}