package com.trie.ecommerce.dto.response;

import com.trie.ecommerce.enums.OrderStatus;

import java.math.BigDecimal;

public record CheckoutResponse(
    Long orderId,
    OrderStatus status,
    String whatsappLink,
    BigDecimal totalAmount
) {}
