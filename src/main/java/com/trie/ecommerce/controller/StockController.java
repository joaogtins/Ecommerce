package com.trie.ecommerce.controller;

import com.trie.ecommerce.dto.request.StockMovementRequest;
import com.trie.ecommerce.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/products/{productId}/stock")
@RequiredArgsConstructor
@Tag(name = "Estoque", description = "Movimentacoes de estoque")
public class StockController {

    private final StockService stockService;

    @PostMapping("/movements")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar movimentacao de estoque")
    @ApiResponse(responseCode = "201", description = "Movimentacao registrada")
    @ApiResponse(responseCode = "409", description = "Estoque insuficiente")
    public void registerMovement(@PathVariable Long productId,
                                  @Valid @RequestBody StockMovementRequest request) {
        stockService.registerMovement(request);
    }

    @GetMapping
    @Operation(summary = "Consultar saldo de estoque do produto")
    public Map<String, Integer> getStock(@PathVariable Long productId) {
        return Map.of("productId", productId.intValue());
    }
}
