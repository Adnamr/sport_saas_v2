package com.sportsaas.inventory.api.dto;

import com.sportsaas.inventory.domain.MovementType;
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
public class StockMovementResponse {
    private UUID id;
    private UUID productId;
    private String productName;
    private MovementType type;
    private int quantity;
    private int quantityBefore;
    private int quantityAfter;
    private String reference;
    private UUID reservationId;
    private String reason;
    private UUID performedBy;
    private LocalDateTime createdAt;
}
