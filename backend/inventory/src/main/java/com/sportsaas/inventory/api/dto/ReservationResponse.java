package com.sportsaas.inventory.api.dto;

import com.sportsaas.inventory.domain.ReservationStatus;
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
public class ReservationResponse {
    private UUID id;
    private UUID productId;
    private String productName;
    private String productSku;
    private int quantity;
    private ReservationStatus status;
    private LocalDateTime expiresAt;
    private String reference;
    private UUID customerId;
    private String notes;
    private boolean expired;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
