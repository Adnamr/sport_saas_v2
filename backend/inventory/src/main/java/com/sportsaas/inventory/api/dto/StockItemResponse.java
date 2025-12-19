package com.sportsaas.inventory.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockItemResponse {
    private UUID id;
    private UUID productId;
    private String productName;
    private String productSku;
    private int availableQuantity;
    private int reservedQuantity;
    private int physicalQuantity;
    private int lowStockThreshold;
    private String location;
    private boolean lowStock;
    private boolean outOfStock;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
