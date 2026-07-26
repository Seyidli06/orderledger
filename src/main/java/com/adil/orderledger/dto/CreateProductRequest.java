package com.adil.orderledger.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


public record CreateProductRequest(
        @NotBlank String name,
        @NotNull @Positive BigDecimal unitPrice,
        @NotNull @Min(0) Integer stockQuantity
) {}
