package com.sportsaas.order.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateQuantityRequest {

    @NotNull(message = "La quantite est obligatoire")
    @Positive(message = "La quantite doit etre positive")
    private Integer quantity;
}
