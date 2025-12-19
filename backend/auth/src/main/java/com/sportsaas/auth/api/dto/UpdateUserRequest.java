package com.sportsaas.auth.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO pour la mise a jour d'un utilisateur.
 */
public record UpdateUserRequest(
    @NotBlank(message = "Le prenom est obligatoire")
    String firstName,

    @NotBlank(message = "Le nom est obligatoire")
    String lastName
) {}
