package com.trie.ecommerce.dto.request;

import com.trie.ecommerce.enums.StockMovementType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record StockMovementRequest(
    @NotNull Long variantId,
    @NotNull StockMovementType type,
    @Positive Integer quantity,
    String reason
) {}
