package com.sportsaas.billing.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateInvoiceRequest {

    @NotNull(message = "L'ID du client est obligatoire")
    private UUID customerId;

    private String customerName;

    private String customerEmail;

    private String customerPhone;

    private String customerAddress;

    private LocalDate dueDate;

    private BigDecimal taxRate;

    private String notes;

    private String paymentTerms;
}
