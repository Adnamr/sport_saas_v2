package com.sportsaas.order.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRentalRequest {

    @NotNull(message = "L'ID du produit est obligatoire")
    private UUID productId;

    @NotNull(message = "L'ID du client est obligatoire")
    private UUID customerId;

    private String customerName;

    private String customerEmail;

    private String customerPhone;

    @Positive(message = "La quantite doit etre positive")
    private int quantity = 1;

    @NotNull(message = "La date de debut est obligatoire")
    private LocalDateTime startDate;

    @NotNull(message = "La date de fin est obligatoire")
    private LocalDateTime endDate;

    @NotNull(message = "Le tarif journalier est obligatoire")
    @Positive(message = "Le tarif doit etre positif")
    private BigDecimal dailyRate;

    @PositiveOrZero(message = "La caution ne peut pas etre negative")
    private BigDecimal deposit = BigDecimal.ZERO;

    private String notes;
}
