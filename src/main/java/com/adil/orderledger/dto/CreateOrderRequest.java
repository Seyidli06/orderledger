package com.adil.orderledger.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CreateOrderRequest(
        @NotEmpty(message = "Order items list cannot be empty")
        List<OrderItemRequest> items,

        String couponCode
) {}