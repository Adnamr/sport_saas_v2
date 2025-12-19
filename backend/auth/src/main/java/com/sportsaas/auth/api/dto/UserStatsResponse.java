package com.sportsaas.auth.api.dto;

/**
 * DTO pour les statistiques utilisateurs.
 */
public record UserStatsResponse(
    long total,
    long admins,
    long employees,
    long customers
) {}
