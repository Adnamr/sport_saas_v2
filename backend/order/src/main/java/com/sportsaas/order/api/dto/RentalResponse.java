package com.sportsaas.order.api.dto;

import com.sportsaas.order.domain.RentalStatus;
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
public class RentalResponse {
    private UUID id;
    private String rentalNumber;
    private UUID orderId;
    private UUID productId;
    private String productName;
    private UUID customerId;
    private String customerName;
    private String customerEmail;
    private RentalStatus status;
    private int quantity;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime actualReturnDate;
    private BigDecimal dailyRate;
    private int rentalDays;
    private BigDecimal deposit;
    private BigDecimal subtotal;
    private BigDecimal extraCharges;
    private String extraChargesNotes;
    private BigDecimal total;
    private String currency;
    private String notes;
    private String conditionAtStart;
    private String conditionAtReturn;
    private boolean overdue;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
