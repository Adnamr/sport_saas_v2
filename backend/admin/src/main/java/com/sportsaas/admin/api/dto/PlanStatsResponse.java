package com.sportsaas.admin.api.dto;

import com.sportsaas.admin.domain.SubscriptionPlan;

/**
 * DTO pour les statistiques par plan.
 */
public record PlanStatsResponse(
    SubscriptionPlan plan,
    long count
) {}
