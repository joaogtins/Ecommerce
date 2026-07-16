package com.trie.ecommerce.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AddItemRequest(
    @NotNull Long variantId,
    @NotNull @Positive Integer quantity
) {}
