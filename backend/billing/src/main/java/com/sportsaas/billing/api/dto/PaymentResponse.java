package com.sportsaas.billing.api.dto;

import com.sportsaas.billing.domain.PaymentMethod;
import com.sportsaas.billing.domain.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private UUID id;
    private String paymentReference;
    private UUID invoiceId;
    private String invoiceNumber;
    private UUID customerId;
    private String customerName;
    private PaymentStatus status;
    private PaymentMethod method;
    private BigDecimal amount;
    private String currency;
    private String externalReference;
    private String description;
    private String notes;
    private BigDecimal refundedAmount;
    private LocalDateTime processedAt;
    private LocalDateTime failedAt;
    private String failureReason;
    private LocalDateTime refundedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
