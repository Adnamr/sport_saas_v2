package com.sportsaas.tenant.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour la création d'un Tenant.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTenantRequest {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 255, message = "Le nom doit faire entre 2 et 255 caractères")
    private String name;

    @Size(max = 100, message = "Le slug doit faire au maximum 100 caractères")
    private String slug;

    @Size(max = 255, message = "Le domaine doit faire au maximum 255 caractères")
    private String domain;
}
