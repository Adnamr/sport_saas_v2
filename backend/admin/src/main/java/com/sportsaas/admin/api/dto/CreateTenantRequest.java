package com.sportsaas.admin.api.dto;

import com.sportsaas.admin.domain.SubscriptionPlan;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

/**
 * DTO pour la creation d'un tenant.
 */
public record CreateTenantRequest(
    @NotBlank(message = "Le nom de l'entreprise est obligatoire")
    String companyName,

    @NotBlank(message = "L'email de contact est obligatoire")
    @Email(message = "L'email doit etre valide")
    String contactEmail,

    String contactPhone,

    String address,

    SubscriptionPlan subscriptionPlan,

    LocalDate subscriptionEnd,

    Integer maxUsers,

    Integer maxProducts,

    Integer maxStorageMb,

    String notes
) {}
