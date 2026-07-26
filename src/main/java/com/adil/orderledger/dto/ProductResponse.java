package com.adil.orderledger.dto;

import java.math.BigDecimal;

public record ProductResponse(
        Long id, String name, BigDecimal unitPrice, Integer stockQuantity, Long version
) {}
