package com.trie.ecommerce.service;

import com.trie.ecommerce.dto.request.CreateProductRequest;
import com.trie.ecommerce.dto.request.CreateVariantRequest;
import com.trie.ecommerce.dto.request.UpdateProductRequest;
import com.trie.ecommerce.dto.response.ProductResponse;
import com.trie.ecommerce.dto.response.VariantResponse;
import com.trie.ecommerce.entity.Product;
import com.trie.ecommerce.entity.ProductVariant;
import com.trie.ecommerce.enums.PricingType;
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
    private final StockService stockService;

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
            request.material(), request.pricingType(), request.pricePerGram(),
            request.imageUrl(), request.featured(), request.newCollection(), variants
        );

        Product product = ProductMapper.toEntity(updatedRequest);
        product = productRepository.save(product);
        return ProductMapper.toResponse(product);
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Produto nao encontrado: " + id));
        return populateStock(ProductMapper.toResponse(product));
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findAll() {
        return productRepository.findByActiveTrue().stream()
            .map(ProductMapper::toResponse)
            .map(this::populateStock)
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
        if (request.imageUrl() != null) product.setImageUrl(request.imageUrl());
        if (request.featured() != null) product.setFeatured(request.featured());
        if (request.newCollection() != null) product.setNewCollection(request.newCollection());

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

    @Transactional(readOnly = true)
    public List<ProductResponse> findFeatured() {
        return productRepository.findByFeaturedTrueAndActiveTrue().stream()
            .map(ProductMapper::toResponse)
            .map(this::populateStock)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findNewCollection() {
        return productRepository.findByNewCollectionTrueAndActiveTrue().stream()
            .map(ProductMapper::toResponse)
            .map(this::populateStock)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> search(String query) {
        return productRepository
            .findByNameContainingIgnoreCaseOrCategoryContainingIgnoreCase(query, query)
            .stream()
            .map(ProductMapper::toResponse)
            .map(this::populateStock)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<String> listCategories() {
        return productRepository.findDistinctActiveCategories();
    }

    private ProductResponse populateStock(ProductResponse response) {
        List<VariantResponse> variants = response.variants().stream()
            .map(v -> v.withStock(stockService.calculateCurrentStock(v.id())))
            .toList();
        return new ProductResponse(
            response.id(), response.name(), response.description(),
            response.category(), response.material(), response.pricingType(),
            response.pricePerGram(), response.active(), response.imageUrl(),
            response.featured(), response.newCollection(),
            response.createdAt(), variants
        );
    }

    private String generateSku() {
        return "SKU-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
