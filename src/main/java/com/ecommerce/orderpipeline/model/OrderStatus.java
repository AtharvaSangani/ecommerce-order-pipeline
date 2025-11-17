package com.ecommerce.orderpipeline.model;

public enum OrderStatus {
    PLACED,
    VALIDATED,
    VALIDATION_FAILED,
    INVENTORY_RESERVED,
    INVENTORY_RESERVATION_FAILED,
    PAYMENT_PROCESSED,
    PAYMENT_FAILED,
    CONFIRMED,
    CANCELLED
}