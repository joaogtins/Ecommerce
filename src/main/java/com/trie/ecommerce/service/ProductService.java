package com.trie.ecommerce.service;

import com.trie.ecommerce.dto.request.CreateProductRequest;
import com.trie.ecommerce.dto.request.CreateVariantRequest;
import com.trie.ecommerce.dto.request.UpdateProductRequest;
import com.trie.ecommerce.dto.response.ProductResponse;
import com.trie.ecommerce.entity.Product;
import com.trie.ecommerce.entity.ProductVariant;
import com.trie.ecommerce.exception.ResourceNotFoundException;
import com.trie.ecommerce.mapper.ProductMapper;
import com.trie.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public ProductResponse create(CreateProductRequest request) {
        List<CreateVariantRequest> variants = request.variants().stream()
            .map(v -> {
                if (v.sku() == null || v.sku().isBlank()) {
                    return new CreateVariantRequest(
                        v.size(), v.weightInGrams(),
                        generateSku(), v.price(), v.isUniquePiece()
                    );
                }
                return v;
            })
            .toList();

        CreateProductRequest updatedRequest = new CreateProductRequest(
            request.name(), request.description(), request.category(),
            request.material(), request.pricingType(), request.pricePerGram(), variants
        );

        Product product = ProductMapper.toEntity(updatedRequest);
        product = productRepository.save(product);
        return ProductMapper.toResponse(product);
    }

    public ProductResponse findById(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Produto nao encontrado: " + id));
        return ProductMapper.toResponse(product);
    }

    public List<ProductResponse> findAll() {
        return productRepository.findByActiveTrue().stream()
            .map(ProductMapper::toResponse)
            .toList();
    }

    @Transactional
    public ProductResponse update(Long id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Produto nao encontrado: " + id));

        if (request.name() != null) product.setName(request.name());
        if (request.description() != null) product.setDescription(request.description());
        if (request.category() != null) product.setCategory(request.category());
        if (request.material() != null) product.setMaterial(request.material());
        if (request.pricingType() != null)
            product.setPricingType(com.trie.ecommerce.enums.PricingType.valueOf(request.pricingType()));
        if (request.pricePerGram() != null) product.setPricePerGram(request.pricePerGram());
        if (request.active() != null) product.setActive(request.active());

        product = productRepository.save(product);
        return ProductMapper.toResponse(product);
    }

    @Transactional
    public void delete(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Produto nao encontrado: " + id));
        product.setActive(false);
        productRepository.save(product);
    }

    private String generateSku() {
        return "SKU-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
