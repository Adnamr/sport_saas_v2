package com.sportsaas.admin.api.dto;

import com.sportsaas.admin.domain.SubscriptionPlan;
import com.sportsaas.admin.domain.TenantStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de reponse pour un tenant.
 */
public record TenantInfoResponse(
    UUID id,
    String companyName,
    String contactEmail,
    String contactPhone,
    String address,
    TenantStatus status,
    SubscriptionPlan subscriptionPlan,
    LocalDate subscriptionStart,
    LocalDate subscriptionEnd,
    Integer maxUsers,
    Integer maxProducts,
    Integer maxStorageMb,
    Integer usedStorageMb,
    String notes,
    LocalDateTime activatedAt,
    LocalDateTime suspendedAt,
    String suspensionReason,
    boolean subscriptionExpired,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
