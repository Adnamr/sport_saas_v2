package com.sportsaas.inventory.api;

import com.sportsaas.inventory.api.dto.*;
import com.sportsaas.inventory.domain.Reservation;
import com.sportsaas.inventory.domain.StockItem;
import com.sportsaas.inventory.domain.StockMovement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InventoryMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "productSku", source = "product.sku")
    @Mapping(target = "lowStock", expression = "java(stockItem.isLowStock())")
    @Mapping(target = "outOfStock", expression = "java(stockItem.isOutOfStock())")
    StockItemResponse toStockItemResponse(StockItem stockItem);

    List<StockItemResponse> toStockItemResponseList(List<StockItem> stockItems);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    StockMovementResponse toStockMovementResponse(StockMovement movement);

    List<StockMovementResponse> toStockMovementResponseList(List<StockMovement> movements);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "productSku", source = "product.sku")
    @Mapping(target = "expired", expression = "java(reservation.isExpired())")
    ReservationResponse toReservationResponse(Reservation reservation);

    List<ReservationResponse> toReservationResponseList(List<Reservation> reservations);
}
