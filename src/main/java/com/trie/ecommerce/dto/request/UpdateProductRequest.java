package com.trie.ecommerce.dto.request;

import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record UpdateProductRequest(
    String name,
    String description,
    String category,
    String material,
    String pricingType,
    @Positive BigDecimal pricePerGram,
    Boolean active,
    String imageUrl,
    Boolean featured,
    Boolean newCollection
) {}
