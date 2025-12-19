package com.sportsaas.auth.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO pour completer une invitation.
 */
public record CompleteInvitationRequest(
    @NotBlank(message = "Le token est obligatoire")
    String token,

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caracteres")
    String password
) {}
