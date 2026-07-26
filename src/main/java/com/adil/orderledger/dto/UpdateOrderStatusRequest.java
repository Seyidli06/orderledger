package com.adil.orderledger.dto;

import com.adil.orderledger.model.OrderStatus;
import org.antlr.v4.runtime.misc.NotNull;

public record UpdateOrderStatusRequest(
        @NotNull OrderStatus newStatus,
        String reason
) {}