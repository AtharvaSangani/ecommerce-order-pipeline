package com.ecommerce.orderpipeline.service;

import com.ecommerce.orderpipeline.model.Order;
import com.ecommerce.orderpipeline.model.OrderStatus;
import com.ecommerce.orderpipeline.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    
    @Mock
    private OrderRepository orderRepository;
    
    @InjectMocks
    private OrderService orderService;
    
    @Test
    void testValidateOrder_Success() {
        // Given
        Order order = createValidOrder();
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        
        // When
        Order result = orderService.validateOrder(order);
        
        // Then
        assertNotNull(result);
        assertEquals(OrderStatus.VALIDATED, result.getStatus());
        verify(orderRepository, times(1)).save(order);
    }
    
    @Test
    void testValidateOrder_InvalidCustomer() {
        // Given
        Order order = createValidOrder();
        order.setCustomerId(null);
        
        // When & Then
        assertThrows(RuntimeException.class, () -> orderService.validateOrder(order));
    }
    
    private Order createValidOrder() {
        Order order = new Order();
        order.setOrderId("TEST-123");
        order.setCustomerId("CUST-001");
        order.setTotalAmount(100.0);
        // ... set other required fields
        return order;
    }
}