package com.sportsaas.billing.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateInvoiceItemRequest {

    @Positive(message = "La quantite doit etre positive")
    private int quantity;

    @NotNull(message = "Le prix unitaire est obligatoire")
    @Positive(message = "Le prix doit etre positif")
    private BigDecimal unitPrice;
}
