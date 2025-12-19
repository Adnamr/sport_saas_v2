package com.sportsaas.billing.api.dto;

import com.sportsaas.billing.domain.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentRequest {

    @NotNull(message = "L'ID de la facture est obligatoire")
    private UUID invoiceId;

    @NotNull(message = "Le montant est obligatoire")
    @Positive(message = "Le montant doit etre positif")
    private BigDecimal amount;

    @NotNull(message = "La methode de paiement est obligatoire")
    private PaymentMethod method;

    private String description;

    private String notes;
}
