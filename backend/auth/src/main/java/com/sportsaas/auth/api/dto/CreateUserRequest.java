package com.sportsaas.auth.api.dto;

import com.sportsaas.auth.domain.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO pour la creation d'un utilisateur.
 */
public record CreateUserRequest(
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit etre valide")
    String email,

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caracteres")
    String password,

    @NotBlank(message = "Le prenom est obligatoire")
    String firstName,

    @NotBlank(message = "Le nom est obligatoire")
    String lastName,

    @NotNull(message = "Le role est obligatoire")
    UserRole role
) {}
