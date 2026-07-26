package com.adil.orderledger.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record UpdateProductRequest(
        @NotBlank(message = "Product name cannot be blank")
        String name,

        @NotNull(message = "Unit price is required")
        @Positive(message = "Unit price must be greater than 0")
        BigDecimal unitPrice,

        @NotNull(message = "Stock quantity is required")
        @Min(value = 0, message = "Stock quantity cannot be negative")
        Integer stockQuantity
) {
}