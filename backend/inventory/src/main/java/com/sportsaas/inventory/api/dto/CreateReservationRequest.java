package com.sportsaas.inventory.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateReservationRequest {

    @NotNull(message = "L'ID du produit est obligatoire")
    private UUID productId;

    @NotNull(message = "La quantite est obligatoire")
    @Positive(message = "La quantite doit etre positive")
    private Integer quantity;

    /** Duree de la reservation en minutes (defaut: 30) */
    private Integer durationMinutes = 30;

    private String reference;

    private UUID customerId;

    private String notes;
}
