package com.sportsaas.order.api.dto;

import com.sportsaas.order.domain.OrderType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    @NotNull(message = "L'ID du client est obligatoire")
    private UUID customerId;

    private String customerName;

    private String customerEmail;

    private String customerPhone;

    private OrderType type = OrderType.SALE;

    private String shippingAddress;

    private String billingAddress;

    private String customerNotes;
}
