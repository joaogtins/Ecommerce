package com.trie.ecommerce.mapper;

import com.trie.ecommerce.dto.response.OrderItemResponse;
import com.trie.ecommerce.dto.response.OrderResponse;
import com.trie.ecommerce.entity.Order;
import com.trie.ecommerce.entity.OrderItem;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class OrderMapper {

    private OrderMapper() {}

    public static OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = Optional.ofNullable(order.getItems())
            .orElse(Collections.emptyList())
            .stream()
            .map(OrderMapper::toResponse)
            .toList();

        return new OrderResponse(
            order.getId(),
            order.getCustomer().getId(),
            order.getStatus(),
            order.getTotalAmount(),
            order.getReservedUntil(),
            order.getCreatedAt(),
            items
        );
    }

    public static OrderItemResponse toResponse(OrderItem item) {
        BigDecimal subtotal = item.getPriceAtPurchase()
            .multiply(BigDecimal.valueOf(item.getQuantity()));

        return new OrderItemResponse(
            item.getId(),
            item.getVariant().getId(),
            item.getVariant().getProduct().getName(),
            item.getVariant().getSize(),
            item.getVariant().getSku(),
            item.getQuantity(),
            item.getPriceAtPurchase(),
            subtotal
        );
    }
}
