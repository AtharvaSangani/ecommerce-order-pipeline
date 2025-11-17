package com.ecommerce.orderpipeline.kafka.producer;

import com.ecommerce.orderpipeline.dto.OrderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Component
public class OrderEventProducer {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderEventProducer.class);
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    @Value("${app.kafka.topics.order-placed}")
    private String orderPlacedTopic;
    
    @Value("${app.kafka.topics.order-validated}")
    private String orderValidatedTopic;
    
    @Value("${app.kafka.topics.inventory-reserved}")
    private String inventoryReservedTopic;
    
    @Value("${app.kafka.topics.payment-processed}")
    private String paymentProcessedTopic;
    
    @Value("${app.kafka.topics.order-confirmed}")
    private String orderConfirmedTopic;
    
    @Value("${app.kafka.topics.order-failed}")
    private String orderFailedTopic;
    
    @Value("${app.kafka.topics.dlq-orders}")
    private String dlqOrdersTopic;
    
    public void sendOrderPlacedEvent(OrderEvent event) {
        sendMessage(orderPlacedTopic, event.getOrderId(), event);
    }
    
    public void sendOrderValidatedEvent(OrderEvent event) {
        sendMessage(orderValidatedTopic, event.getOrderId(), event);
    }
    
    public void sendInventoryReservedEvent(OrderEvent event) {
        sendMessage(inventoryReservedTopic, event.getOrderId(), event);
    }
    
    public void sendPaymentProcessedEvent(OrderEvent event) {
        sendMessage(paymentProcessedTopic, event.getOrderId(), event);
    }
    
    public void sendOrderConfirmedEvent(OrderEvent event) {
        sendMessage(orderConfirmedTopic, event.getOrderId(), event);
    }
    
    public void sendOrderFailedEvent(OrderEvent event) {
        sendMessage(orderFailedTopic, event.getOrderId(), event);
    }
    
    public void sendToDLQ(OrderEvent event) {
        sendMessage(dlqOrdersTopic, event.getOrderId(), event);
    }
    
    private void sendMessage(String topic, String key, Object message) {
        ListenableFuture<SendResult<String, Object>> future = 
            kafkaTemplate.send(topic, key, message);
        
        future.addCallback(new ListenableFutureCallback<SendResult<String, Object>>() {
            @Override
            public void onSuccess(SendResult<String, Object> result) {
                logger.info("Sent message to topic: {} with key: {}, offset: {}", 
                    topic, key, result.getRecordMetadata().offset());
            }
            
            @Override
            public void onFailure(Throwable ex) {
                logger.error("Unable to send message to topic: {} with key: {}, error: {}", 
                    topic, key, ex.getMessage());
            }
        });
    }
}