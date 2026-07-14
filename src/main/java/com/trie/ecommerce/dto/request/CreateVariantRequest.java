package com.trie.ecommerce.dto.request;

import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateVariantRequest(
    String size,
    @Positive BigDecimal weightInGrams,
    String sku,
    @Positive BigDecimal price,
    Boolean isUniquePiece
) {}
