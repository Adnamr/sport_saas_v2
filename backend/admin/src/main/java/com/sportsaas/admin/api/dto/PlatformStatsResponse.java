package com.sportsaas.admin.api.dto;

/**
 * DTO pour les statistiques de la plateforme.
 */
public record PlatformStatsResponse(
    long totalTenants,
    long activeTenants,
    long suspendedTenants,
    long trialTenants,
    long totalUsers,
    long totalProducts,
    long totalOrders,
    long totalRentals
) {}
