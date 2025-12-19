package com.sportsaas.order.api.dto;

import com.sportsaas.order.domain.OrderStatus;
import com.sportsaas.order.domain.OrderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private UUID id;
    private String orderNumber;
    private OrderType type;
    private OrderStatus status;
    private UUID customerId;
    private String customerName;
    private String customerEmail;
    private List<OrderItemResponse> items;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private String discountCode;
    private BigDecimal taxAmount;
    private BigDecimal shippingAmount;
    private BigDecimal total;
    private String currency;
    private String shippingAddress;
    private String billingAddress;
    private String notes;
    private LocalDateTime paidAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime cancelledAt;
    private String cancellationReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
