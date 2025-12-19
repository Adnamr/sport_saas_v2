package com.sportsaas.admin.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO pour la suspension d'un tenant.
 */
public record SuspendRequest(
    @NotBlank(message = "La raison de suspension est obligatoire")
    String reason
) {}
