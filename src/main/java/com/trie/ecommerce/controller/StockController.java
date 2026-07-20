package com.trie.ecommerce.controller;

import com.trie.ecommerce.dto.request.StockMovementRequest;
import com.trie.ecommerce.entity.ProductVariant;
import com.trie.ecommerce.repository.ProductVariantRepository;
import com.trie.ecommerce.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products/{productId}/stock")
@RequiredArgsConstructor
@Tag(name = "Estoque", description = "Movimentacoes de estoque")
public class StockController {

    private final StockService stockService;
    private final ProductVariantRepository variantRepository;

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
    @Operation(summary = "Consultar saldo de estoque por variante")
    @ApiResponse(responseCode = "200", description = "Mapa variantId → estoque")
    public Map<Long, Integer> getStock(@PathVariable Long productId) {
        List<ProductVariant> variants = variantRepository.findByProductId(productId);
        Map<Long, Integer> stockMap = new HashMap<>();
        for (ProductVariant v : variants) {
            stockMap.put(v.getId(), stockService.calculateCurrentStock(v.getId()));
        }
        return stockMap;
    }
}
