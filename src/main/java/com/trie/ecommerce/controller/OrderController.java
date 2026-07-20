package com.trie.ecommerce.controller;

import com.trie.ecommerce.dto.request.UpdateStatusRequest;
import com.trie.ecommerce.dto.response.OrderResponse;
import com.trie.ecommerce.dto.response.OrderStatusHistoryResponse;
import com.trie.ecommerce.enums.OrderStatus;
import com.trie.ecommerce.exception.InvalidStatusTransitionException;
import com.trie.ecommerce.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Pedidos", description = "Gestao de pedidos (admin)")
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    @Operation(summary = "Listar todos os pedidos")
    public List<OrderResponse> findAll() {
        return orderService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar pedido por ID")
    @ApiResponse(responseCode = "404", description = "Pedido nao encontrado")
    public OrderResponse findById(@PathVariable Long id) {
        return orderService.findById(id);
    }

    @GetMapping("/{id}/history")
    @Operation(summary = "Historico de status do pedido")
    public List<OrderStatusHistoryResponse> getHistory(@PathVariable Long id) {
        return orderService.getHistory(id);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Atualizar status do pedido")
    @ApiResponse(responseCode = "200", description = "Status atualizado")
    @ApiResponse(responseCode = "422", description = "Transicao invalida")
    public OrderResponse updateStatus(@PathVariable Long id,
                                       @Valid @RequestBody UpdateStatusRequest request) {
        return switch (request.status()) {
            case PAID -> orderService.markAsPaid(id, "admin");
            case PREPARING -> orderService.markAsPreparing(id);
            case OUT_FOR_DELIVERY -> orderService.markAsOutForDelivery(id);
            case DELIVERED -> orderService.markAsDelivered(id);
            case CANCELLED -> orderService.cancelOrder(id, request.reason());
            default -> throw new InvalidStatusTransitionException("Status invalido: " + request.status());
        };
    }
}
