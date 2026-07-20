package com.trie.ecommerce.mapper;

import com.trie.ecommerce.dto.response.OrderItemResponse;
import com.trie.ecommerce.dto.response.OrderResponse;
import com.trie.ecommerce.dto.response.OrderStatusHistoryResponse;
import com.trie.ecommerce.entity.Order;
import com.trie.ecommerce.entity.OrderItem;
import com.trie.ecommerce.entity.OrderStatusHistory;
import com.trie.ecommerce.enums.OrderStatus;

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

        List<OrderStatusHistoryResponse> history = Optional.ofNullable(order.getStatusHistory())
            .orElse(Collections.emptyList())
            .stream()
            .map(OrderMapper::toHistoryResponse)
            .toList();

        return new OrderResponse(
            order.getId(),
            order.getCustomer().getId(),
            order.getStatus(),
            order.getTotalAmount(),
            order.getReservedUntil(),
            order.getCreatedAt(),
            items,
            history
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

    public static OrderStatusHistoryResponse toHistoryResponse(OrderStatusHistory history) {
        return new OrderStatusHistoryResponse(
            history.getId(),
            history.getFromStatus(),
            history.getToStatus(),
            history.getChangedAt(),
            history.getNotes()
        );
    }
}
