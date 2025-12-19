package com.sportsaas.inventory.api.dto;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStockSettingsRequest {

    @PositiveOrZero(message = "Le seuil ne peut pas etre negatif")
    private Integer lowStockThreshold;

    private String location;
}
