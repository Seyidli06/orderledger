package com.adil.orderledger.dto;

import com.adil.orderledger.model.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(
        @NotNull OrderStatus newStatus,
        String reason
) {}