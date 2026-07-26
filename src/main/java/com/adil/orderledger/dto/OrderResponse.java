package com.adil.orderledger.dto;

import com.adil.orderledger.model.OrderStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        OrderStatus status,
        BigDecimal totalAmount,
        List<OrderItemResponse> items,
        OffsetDateTime createdAt,
        String message // <--- Direct message field
) {
    // Convenience constructor for queries where no specific action message is needed
    public OrderResponse(Long id, OrderStatus status, BigDecimal totalAmount, List<OrderItemResponse> items, OffsetDateTime createdAt) {
        this(id, status, totalAmount, items, createdAt, null);
    }
}