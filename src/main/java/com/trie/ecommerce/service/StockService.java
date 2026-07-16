package com.trie.ecommerce.service;

import com.trie.ecommerce.dto.request.StockMovementRequest;
import com.trie.ecommerce.entity.ProductVariant;
import com.trie.ecommerce.entity.StockMovement;
import com.trie.ecommerce.enums.StockMovementType;
import com.trie.ecommerce.exception.InsufficientStockException;
import com.trie.ecommerce.exception.ResourceNotFoundException;
import com.trie.ecommerce.repository.ProductVariantRepository;
import com.trie.ecommerce.repository.StockMovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockMovementRepository stockMovementRepository;
    private final ProductVariantRepository variantRepository;

    @Transactional
    public void registerMovement(StockMovementRequest request) {
        ProductVariant variant = variantRepository.findById(request.variantId())
            .orElseThrow(() -> new ResourceNotFoundException("Variante nao encontrada: " + request.variantId()));

        int currentStock = calculateCurrentStock(variant.getId());

        if (request.type() == StockMovementType.OUT && currentStock < request.quantity()) {
            throw new InsufficientStockException(
                "Estoque insuficiente. Disponivel: " + currentStock + ", solicitado: " + request.quantity());
        }

        StockMovement movement = StockMovement.builder()
            .variant(variant)
            .type(request.type())
            .quantity(request.quantity())
            .reason(request.reason())
            .build();

        stockMovementRepository.save(movement);
    }

    public int calculateCurrentStock(Long variantId) {
        Integer stock = stockMovementRepository.calculateCurrentStock(variantId);
        return stock != null ? stock : 0;
    }

    public int calculatePhysicalStock(Long variantId) {
        Integer stock = stockMovementRepository.calculatePhysicalStock(variantId);
        return stock != null ? stock : 0;
    }
}
