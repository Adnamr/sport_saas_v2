package com.sportsaas.billing.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplyTaxRequest {

    @NotNull(message = "Le taux de TVA est obligatoire")
    @Min(value = 0, message = "Le taux ne peut pas etre negatif")
    @Max(value = 100, message = "Le taux ne peut pas depasser 100%")
    private BigDecimal taxRate;
}
