package com.sportsaas.catalog.domain;

import com.sportsaas.common.domain.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Entite Product - Produit du catalogue.
 */
@Entity
@Table(name = "products", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tenant_id", "sku"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product extends TenantAwareEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(nullable = false)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String shortDescription;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(precision = 10, scale = 2)
    private BigDecimal compareAtPrice;

    @Column(precision = 10, scale = 2)
    private BigDecimal costPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status = ProductStatus.DRAFT;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<ProductImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductAttribute> attributes = new ArrayList<>();

    @Column
    private String brand;

    @Column
    private String barcode;

    @Column(nullable = false)
    private boolean taxable = true;

    @Column(precision = 10, scale = 3)
    private BigDecimal weight;

    @Column
    private String weightUnit = "kg";

    /**
     * Retourne l'image principale.
     */
    public ProductImage getMainImage() {
        return images.stream()
                .filter(ProductImage::isMain)
                .findFirst()
                .orElse(images.isEmpty() ? null : images.get(0));
    }

    /**
     * Verifie si le produit est en promotion.
     */
    public boolean isOnSale() {
        return compareAtPrice != null && compareAtPrice.compareTo(price) > 0;
    }

    /**
     * Calcule le pourcentage de reduction.
     */
    public int getDiscountPercentage() {
        if (!isOnSale()) return 0;
        return compareAtPrice.subtract(price)
                .divide(compareAtPrice, 2, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .intValue();
    }
}
