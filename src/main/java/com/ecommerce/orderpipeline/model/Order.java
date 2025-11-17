package com.ecommerce.orderpipeline.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    private String orderId;
    
    private String customerId;
    private String customerEmail;
    
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    
    private Double totalAmount;
    private String shippingAddress;
    
    @ElementCollection
    private List<OrderItem> items;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private String failureReason;
    private Integer retryCount = 0;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
class OrderItem {
    private String productId;
    private String productName;
    private Integer quantity;
    private Double price;
}