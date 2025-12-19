package com.sportsaas.auth.api.dto;

import com.sportsaas.auth.domain.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO pour l'invitation d'un utilisateur.
 */
public record InviteUserRequest(
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit etre valide")
    String email,

    @NotBlank(message = "Le prenom est obligatoire")
    String firstName,

    @NotBlank(message = "Le nom est obligatoire")
    String lastName,

    @NotNull(message = "Le role est obligatoire")
    UserRole role
) {}
