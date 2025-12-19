package com.sportsaas.dashboard.api.dto;

import java.util.Map;

/**
 * DTO pour la repartition des commandes par statut.
 */
public record OrdersByStatusResponse(
    /** Nombre total de commandes */
    long total,
    /** Repartition par statut */
    Map<String, Long> byStatus
) {}
