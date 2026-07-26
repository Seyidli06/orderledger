package com.adil.orderledger.dto;

import com.adil.orderledger.model.OrderStatus;

import java.time.OffsetDateTime;

public record OrderStatusHistoryResponse(
        Long id, Long orderId, OrderStatus previousStatus, OrderStatus newStatus, String reason, OffsetDateTime createdAt
) {}