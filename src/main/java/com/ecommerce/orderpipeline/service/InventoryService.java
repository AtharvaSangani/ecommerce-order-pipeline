package com.ecommerce.orderpipeline.service;

import com.ecommerce.orderpipeline.model.Order;
import com.ecommerce.orderpipeline.model.ProductInventory;
import com.ecommerce.orderpipeline.repository.InventoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {
    
    private static final Logger logger = LoggerFactory.getLogger(InventoryService.class);
    
    @Autowired
    private InventoryRepository inventoryRepository;
    
    @Transactional
    @Retryable(value = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public boolean reserveInventory(Order order) {
        logger.info("Reserving inventory for order: {}", order.getOrderId());
        
        for (var item : order.getItems()) {
            ProductInventory inventory = inventoryRepository.findByProductId(item.getProductId())
                    .orElseThrow(() -> new RuntimeException(
                        "Product not found: " + item.getProductId()));
            
            if (inventory.getAvailableQuantity() < item.getQuantity()) {
                throw new RuntimeException(
                    "Insufficient inventory for product: " + item.getProductId() + 
                    ". Available: " + inventory.getAvailableQuantity() + 
                    ", Requested: " + item.getQuantity());
            }
            
            // Reserve inventory
            inventory.setAvailableQuantity(inventory.getAvailableQuantity() - item.getQuantity());
            inventory.setReservedQuantity(inventory.getReservedQuantity() + item.getQuantity());
            inventoryRepository.save(inventory);
            
            logger.info("Reserved {} units of product {}", item.getQuantity(), item.getProductId());
        }
        
        logger.info("Inventory reservation completed for order: {}", order.getOrderId());
        return true;
    }
    
    @Transactional
    public void releaseInventory(Order order) {
        logger.info("Releasing inventory for order: {}", order.getOrderId());
        
        for (var item : order.getItems()) {
            ProductInventory inventory = inventoryRepository.findByProductId(item.getProductId())
                    .orElse(null);
            
            if (inventory != null) {
                // Release reserved inventory
                inventory.setAvailableQuantity(inventory.getAvailableQuantity() + item.getQuantity());
                inventory.setReservedQuantity(inventory.getReservedQuantity() - item.getQuantity());
                inventoryRepository.save(inventory);
                
                logger.info("Released {} units of product {}", item.getQuantity(), item.getProductId());
            }
        }
        
        logger.info("Inventory release completed for order: {}", order.getOrderId());
    }
}