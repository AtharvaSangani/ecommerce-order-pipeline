package com.ecommerce.orderpipeline.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.retry.annotation.EnableRetry;

@Configuration
@EnableKafka
@EnableRetry
public class KafkaConfig {
    
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
    
    @Bean
    public NewTopic orderPlacedTopic() {
        return TopicBuilder.name(orderPlacedTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
    
    @Bean
    public NewTopic orderValidatedTopic() {
        return TopicBuilder.name(orderValidatedTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
    
    @Bean
    public NewTopic inventoryReservedTopic() {
        return TopicBuilder.name(inventoryReservedTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
    
    @Bean
    public NewTopic paymentProcessedTopic() {
        return TopicBuilder.name(paymentProcessedTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
    
    @Bean
    public NewTopic orderConfirmedTopic() {
        return TopicBuilder.name(orderConfirmedTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
    
    @Bean
    public NewTopic orderFailedTopic() {
        return TopicBuilder.name(orderFailedTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
    
    @Bean
    public NewTopic dlqOrdersTopic() {
        return TopicBuilder.name(dlqOrdersTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }
}