package com.adil.orderledger.model;

import java.util.Set;

public enum OrderStatus {
    CREATED,
    PAID,
    SHIPPED,
    COMPLETED,
    CANCELLED;

    // Defines valid next transitions for each status
    public boolean canTransitionTo(OrderStatus nextStatus) {
        if (nextStatus == null) {
            return false;
        }

        return switch (this) {
            case CREATED -> Set.of(PAID, CANCELLED).contains(nextStatus);
            case PAID -> Set.of(SHIPPED, CANCELLED).contains(nextStatus);
            case SHIPPED -> Set.of(COMPLETED).contains(nextStatus);
            case COMPLETED, CANCELLED -> false; // Terminal states
        };
    }
}