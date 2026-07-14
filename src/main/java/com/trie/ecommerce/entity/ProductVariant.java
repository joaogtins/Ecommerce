package com.trie.ecommerce.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "product_variants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Product product;

    private String size;

    @Positive
    @Column(precision = 10, scale = 4)
    private BigDecimal weightInGrams;

    @Positive
    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @NotBlank
    @Column(unique = true, nullable = false)
    private String sku;

    @Builder.Default
    private Boolean isUniquePiece = false;

    @Transient
    private Integer stockQuantity;

    @Version
    private Long version;

    @Builder.Default
    private Boolean active = true;
}
