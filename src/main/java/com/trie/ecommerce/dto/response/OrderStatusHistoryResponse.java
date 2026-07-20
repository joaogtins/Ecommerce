package com.trie.ecommerce.dto.response;

import com.trie.ecommerce.enums.OrderStatus;

import java.time.LocalDateTime;

public record OrderStatusHistoryResponse(
    Long id,
    OrderStatus fromStatus,
    OrderStatus toStatus,
    LocalDateTime changedAt,
    String notes
) {}
