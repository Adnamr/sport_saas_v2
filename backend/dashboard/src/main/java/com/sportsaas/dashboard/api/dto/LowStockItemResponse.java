package com.sportsaas.dashboard.api.dto;

import java.util.UUID;

/**
 * DTO pour un produit en stock bas.
 */
public record LowStockItemResponse(
    UUID productId,
    String productName,
    String productSku,
    int availableQuantity,
    int lowStockThreshold,
    String location,
    boolean isOutOfStock
) {}
