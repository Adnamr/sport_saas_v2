package com.sportsaas.admin.api.dto;

import com.sportsaas.admin.domain.SubscriptionPlan;
import jakarta.validation.constraints.NotNull;

/**
 * DTO pour le changement de plan.
 */
public record ChangePlanRequest(
    @NotNull(message = "Le plan est obligatoire")
    SubscriptionPlan plan
) {}
