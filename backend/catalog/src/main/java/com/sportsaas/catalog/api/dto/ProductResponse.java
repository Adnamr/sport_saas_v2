package com.sportsaas.catalog.api.dto;

import com.sportsaas.catalog.domain.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private UUID id;
    private String name;
    private String sku;
    private String slug;
    private String description;
    private String shortDescription;
    private BigDecimal price;
    private BigDecimal compareAtPrice;
    private ProductStatus status;
    private UUID categoryId;
    private String categoryName;
    private String brand;
    private String barcode;
    private boolean taxable;
    private BigDecimal weight;
    private String weightUnit;
    private boolean onSale;
    private int discountPercentage;
    private String mainImageUrl;
    private List<ProductImageResponse> images;
    private List<ProductAttributeResponse> attributes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
