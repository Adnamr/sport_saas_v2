package com.sportsaas.order.api;

import com.sportsaas.order.api.dto.*;
import com.sportsaas.order.domain.Order;
import com.sportsaas.order.domain.OrderItem;
import com.sportsaas.order.domain.Rental;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    OrderResponse toOrderResponse(Order order);

    List<OrderResponse> toOrderResponseList(List<Order> orders);

    @Mapping(target = "productId", source = "product.id")
    OrderItemResponse toOrderItemResponse(OrderItem item);

    List<OrderItemResponse> toOrderItemResponseList(List<OrderItem> items);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "orderNumber", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "subtotal", ignore = true)
    @Mapping(target = "discountAmount", ignore = true)
    @Mapping(target = "discountCode", ignore = true)
    @Mapping(target = "taxAmount", ignore = true)
    @Mapping(target = "shippingAmount", ignore = true)
    @Mapping(target = "total", ignore = true)
    @Mapping(target = "currency", ignore = true)
    @Mapping(target = "notes", ignore = true)
    @Mapping(target = "paidAt", ignore = true)
    @Mapping(target = "deliveredAt", ignore = true)
    @Mapping(target = "cancelledAt", ignore = true)
    @Mapping(target = "cancellationReason", ignore = true)
    Order toOrder(CreateOrderRequest request);

    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "overdue", expression = "java(rental.isOverdue())")
    RentalResponse toRentalResponse(Rental rental);

    List<RentalResponse> toRentalResponseList(List<Rental> rentals);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "rentalNumber", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "productName", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "actualReturnDate", ignore = true)
    @Mapping(target = "rentalDays", ignore = true)
    @Mapping(target = "subtotal", ignore = true)
    @Mapping(target = "extraCharges", ignore = true)
    @Mapping(target = "extraChargesNotes", ignore = true)
    @Mapping(target = "total", ignore = true)
    @Mapping(target = "currency", ignore = true)
    @Mapping(target = "conditionAtStart", ignore = true)
    @Mapping(target = "conditionAtReturn", ignore = true)
    Rental toRental(CreateRentalRequest request);
}
