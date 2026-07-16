package com.trie.ecommerce.dto.response;

import com.trie.ecommerce.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
    Long id,
    Long customerId,
    OrderStatus status,
    BigDecimal totalAmount,
    LocalDateTime reservedUntil,
    LocalDateTime createdAt,
    List<OrderItemResponse> items
) {}
