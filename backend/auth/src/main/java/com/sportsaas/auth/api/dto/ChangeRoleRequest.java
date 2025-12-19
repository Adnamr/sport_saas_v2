package com.sportsaas.auth.api.dto;

import com.sportsaas.auth.domain.UserRole;
import jakarta.validation.constraints.NotNull;

/**
 * DTO pour le changement de role d'un utilisateur.
 */
public record ChangeRoleRequest(
    @NotNull(message = "Le role est obligatoire")
    UserRole role
) {}
