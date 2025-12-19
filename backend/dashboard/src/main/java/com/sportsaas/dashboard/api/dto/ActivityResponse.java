package com.sportsaas.dashboard.api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO pour une activite recente.
 */
public record ActivityResponse(
    UUID id,
    String type,
    String description,
    UUID userId,
    String userName,
    String entityType,
    UUID entityId,
    LocalDateTime createdAt
) {}
