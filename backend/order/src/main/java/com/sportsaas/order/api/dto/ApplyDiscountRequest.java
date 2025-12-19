package com.sportsaas.order.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplyDiscountRequest {

    @NotBlank(message = "Le code de remise est obligatoire")
    private String discountCode;

    @NotNull(message = "Le montant est obligatoire")
    @PositiveOrZero(message = "Le montant ne peut pas etre negatif")
    private BigDecimal amount;
}
