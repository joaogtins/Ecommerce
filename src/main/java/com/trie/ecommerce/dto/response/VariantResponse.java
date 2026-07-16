package com.trie.ecommerce.dto.response;

import java.math.BigDecimal;

public record VariantResponse(
    Long id,
    String size,
    BigDecimal weightInGrams,
    BigDecimal price,
    String sku,
    Boolean isUniquePiece,
    Integer stockQuantity
) {
    public VariantResponse withStock(Integer stock) {
        return new VariantResponse(id, size, weightInGrams, price, sku, isUniquePiece, stock);
    }
}
