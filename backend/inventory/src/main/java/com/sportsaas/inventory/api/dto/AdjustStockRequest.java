package com.sportsaas.inventory.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdjustStockRequest {

    @NotNull(message = "L'ID du produit est obligatoire")
    private UUID productId;

    @NotNull(message = "La nouvelle quantite est obligatoire")
    @PositiveOrZero(message = "La quantite ne peut pas etre negative")
    private Integer newQuantity;

    private String reason;
}
