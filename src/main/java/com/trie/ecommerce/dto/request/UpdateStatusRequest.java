package com.trie.ecommerce.dto.request;

import com.trie.ecommerce.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(
    @NotNull OrderStatus status,
    String reason
) {}
