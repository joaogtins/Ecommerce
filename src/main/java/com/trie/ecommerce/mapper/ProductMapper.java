package com.trie.ecommerce.mapper;

import com.trie.ecommerce.dto.request.CreateProductRequest;
import com.trie.ecommerce.dto.request.CreateVariantRequest;
import com.trie.ecommerce.dto.response.ProductResponse;
import com.trie.ecommerce.dto.response.VariantResponse;
import com.trie.ecommerce.entity.Product;
import com.trie.ecommerce.entity.ProductVariant;
import com.trie.ecommerce.enums.PricingType;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ProductMapper {

    private ProductMapper() {}

    public static ProductResponse toResponse(Product product) {
        return new ProductResponse(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getCategory(),
            product.getMaterial(),
            product.getPricingType(),
            product.getPricePerGram(),
            product.getActive(),
            product.getCreatedAt(),
            Optional.ofNullable(product.getVariants())
                .orElse(Collections.emptyList())
                .stream()
                .map(ProductMapper::toResponse)
                .toList()
        );
    }

    public static VariantResponse toResponse(ProductVariant variant) {
        return new VariantResponse(
            variant.getId(),
            variant.getSize(),
            variant.getWeightInGrams(),
            variant.getPrice(),
            variant.getSku(),
            variant.getIsUniquePiece(),
            variant.getStockQuantity()
        );
    }

    public static Product toEntity(CreateProductRequest request) {
        Product product = Product.builder()
            .name(request.name())
            .description(request.description())
            .category(request.category())
            .material(request.material())
            .pricingType(request.pricingType())
            .pricePerGram(request.pricingType() == PricingType.BY_GRAM
                ? request.pricePerGram() : null)
            .active(true)
            .build();

        List<ProductVariant> variants = Optional.ofNullable(request.variants())
            .orElse(Collections.emptyList())
            .stream()
            .map(v -> toEntity(v, product))
            .toList();

        product.setVariants(variants);
        return product;
    }

    public static ProductVariant toEntity(CreateVariantRequest request, Product product) {
        return ProductVariant.builder()
            .product(product)
            .size(request.size())
            .weightInGrams(request.weightInGrams())
            .price(request.price())
            .sku(request.sku())
            .isUniquePiece(request.isUniquePiece() != null && request.isUniquePiece())
            .active(true)
            .build();
    }
}
