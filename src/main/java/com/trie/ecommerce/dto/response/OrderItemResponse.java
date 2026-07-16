package com.trie.ecommerce.dto.response;

import java.math.BigDecimal;

public record OrderItemResponse(
    Long id,
    Long variantId,
    String productName,
    String variantSize,
    String sku,
    Integer quantity,
    BigDecimal priceAtPurchase,
    BigDecimal subtotal
) {}
