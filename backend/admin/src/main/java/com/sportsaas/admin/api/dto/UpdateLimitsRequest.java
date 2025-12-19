package com.sportsaas.admin.api.dto;

import jakarta.validation.constraints.Min;

/**
 * DTO pour la mise a jour des limites.
 */
public record UpdateLimitsRequest(
    @Min(value = 1, message = "Le nombre minimum d'utilisateurs est 1")
    Integer maxUsers,

    @Min(value = 1, message = "Le nombre minimum de produits est 1")
    Integer maxProducts,

    @Min(value = 1, message = "Le stockage minimum est 1 Mo")
    Integer maxStorageMb
) {}
