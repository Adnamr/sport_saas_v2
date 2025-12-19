package com.sportsaas.dashboard.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO pour une commande recente.
 */
public record RecentOrderResponse(
    UUID id,
    String orderNumber,
    String customerName,
    String status,
    String type,
    BigDecimal total,
    String currency,
    LocalDateTime createdAt
) {}
