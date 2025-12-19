package com.sportsaas.admin.api.dto;

import jakarta.validation.constraints.Email;

/**
 * DTO pour la mise a jour d'un tenant.
 */
public record UpdateTenantRequest(
    String companyName,

    @Email(message = "L'email doit etre valide")
    String contactEmail,

    String contactPhone,

    String address,

    String notes
) {}
