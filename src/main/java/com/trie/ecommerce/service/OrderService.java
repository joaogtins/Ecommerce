package com.trie.ecommerce.service;

import com.trie.ecommerce.dto.response.OrderResponse;
import com.trie.ecommerce.dto.response.OrderStatusHistoryResponse;
import com.trie.ecommerce.entity.*;
import com.trie.ecommerce.enums.OrderStatus;
import com.trie.ecommerce.enums.PaymentStatus;
import com.trie.ecommerce.enums.StockMovementType;
import com.trie.ecommerce.exception.InvalidStatusTransitionException;
import com.trie.ecommerce.exception.ResourceNotFoundException;
import com.trie.ecommerce.mapper.OrderMapper;
import com.trie.ecommerce.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository statusHistoryRepository;
    private final StockMovementRepository stockMovementRepository;
    private final PaymentRepository paymentRepository;

    private static final Set<OrderStatus> CANCELLABLE_STATUSES = Set.of(
        OrderStatus.PENDING, OrderStatus.PAID, OrderStatus.PREPARING,
        OrderStatus.OUT_FOR_DELIVERY
    );

    @Transactional(readOnly = true)
    public OrderResponse findById(Long id) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Pedido nao encontrado: " + id));
        return OrderMapper.toResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findAll() {
        return orderRepository.findAll().stream()
            .map(OrderMapper::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderStatusHistoryResponse> getHistory(Long id) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Pedido nao encontrado: " + id));
        return order.getStatusHistory().stream()
            .map(OrderMapper::toHistoryResponse)
            .toList();
    }

    @Transactional
    public OrderResponse markAsPaid(Long orderId, String confirmedBy) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Pedido nao encontrado: " + orderId));

        validateTransition(order.getStatus(), OrderStatus.PAID, Set.of(OrderStatus.PENDING));

        for (OrderItem item : order.getItems()) {
            StockMovement out = StockMovement.builder()
                .variant(item.getVariant())
                .type(StockMovementType.OUT)
                .quantity(item.getQuantity())
                .reason("Pagamento confirmado - Pedido #" + orderId)
                .orderReference(orderId.toString())
                .build();
            stockMovementRepository.save(out);
        }

        Payment payment = order.getPayment();
        if (payment != null) {
            payment.setStatus(PaymentStatus.APPROVED);
            payment.setConfirmedBy(confirmedBy);
            payment.setConfirmedAt(java.time.LocalDateTime.now());
            paymentRepository.save(payment);
        }

        order.setStatus(OrderStatus.PAID);
        order.setUpdatedAt(java.time.LocalDateTime.now());
        saveStatusHistory(order, OrderStatus.PENDING, OrderStatus.PAID,
            "Pagamento confirmado por " + confirmedBy);

        return OrderMapper.toResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse markAsPreparing(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Pedido nao encontrado: " + orderId));
        validateTransition(order.getStatus(), OrderStatus.PREPARING, Set.of(OrderStatus.PAID));
        return transitionStatus(order, OrderStatus.PREPARING, "Separacao iniciada");
    }

    @Transactional
    public OrderResponse markAsOutForDelivery(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Pedido nao encontrado: " + orderId));
        validateTransition(order.getStatus(), OrderStatus.OUT_FOR_DELIVERY, Set.of(OrderStatus.PREPARING));
        return transitionStatus(order, OrderStatus.OUT_FOR_DELIVERY, "Saiu para entrega");
    }

    @Transactional
    public OrderResponse markAsDelivered(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Pedido nao encontrado: " + orderId));
        validateTransition(order.getStatus(), OrderStatus.DELIVERED, Set.of(OrderStatus.OUT_FOR_DELIVERY));
        return transitionStatus(order, OrderStatus.DELIVERED, "Entregue");
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Pedido nao encontrado: " + orderId));

        if (!CANCELLABLE_STATUSES.contains(order.getStatus())) {
            throw new InvalidStatusTransitionException(
                "Pedido nao pode ser cancelado no status " + order.getStatus());
        }

        OrderStatus fromStatus = order.getStatus();

        if (fromStatus == OrderStatus.PENDING || fromStatus == OrderStatus.PAID) {
            for (OrderItem item : order.getItems()) {
                StockMovement release = StockMovement.builder()
                    .variant(item.getVariant())
                    .type(StockMovementType.RELEASE)
                    .quantity(item.getQuantity())
                    .reason("Cancelamento - Pedido #" + orderId)
                    .orderReference(orderId.toString())
                    .build();
                stockMovementRepository.save(release);
            }
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(java.time.LocalDateTime.now());
        saveStatusHistory(order, fromStatus, OrderStatus.CANCELLED, reason);

        if (order.getPayment() != null) {
            Payment payment = order.getPayment();
            payment.setStatus(PaymentStatus.REJECTED);
            paymentRepository.save(payment);
        }

        return OrderMapper.toResponse(orderRepository.save(order));
    }

    private OrderResponse transitionStatus(Order order, OrderStatus to, String notes) {
        OrderStatus from = order.getStatus();
        order.setStatus(to);
        order.setUpdatedAt(java.time.LocalDateTime.now());
        saveStatusHistory(order, from, to, notes);
        return OrderMapper.toResponse(orderRepository.save(order));
    }

    private void validateTransition(OrderStatus current, OrderStatus target, Set<OrderStatus> allowed) {
        if (!allowed.contains(current)) {
            throw new InvalidStatusTransitionException(
                String.format("Transicao invalida: %s → %s", current, target));
        }
    }

    private void saveStatusHistory(Order order, OrderStatus from, OrderStatus to, String notes) {
        OrderStatusHistory history = OrderStatusHistory.builder()
            .order(order)
            .fromStatus(from)
            .toStatus(to)
            .notes(notes)
            .build();
        statusHistoryRepository.save(history);
    }
}
