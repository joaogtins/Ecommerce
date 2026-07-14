package com.trie.ecommerce.dto.response;

import com.trie.ecommerce.enums.PricingType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ProductResponse(
    Long id,
    String name,
    String description,
    String category,
    String material,
    PricingType pricingType,
    BigDecimal pricePerGram,
    Boolean active,
    LocalDateTime createdAt,
    List<VariantResponse> variants
) {}
