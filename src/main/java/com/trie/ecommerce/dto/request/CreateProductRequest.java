package com.trie.ecommerce.dto.request;

import com.trie.ecommerce.enums.PricingType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

public record CreateProductRequest(
    @NotBlank String name,
    String description,
    @NotBlank String category,
    String material,
    @NotNull PricingType pricingType,
    @Positive BigDecimal pricePerGram,
    String imageUrl,
    Boolean featured,
    Boolean newCollection,
    @NotEmpty List<CreateVariantRequest> variants
) {}
