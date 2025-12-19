package com.sportsaas.order.api;

import com.sportsaas.common.dto.PageResponse;
import com.sportsaas.common.exception.NotFoundException;
import com.sportsaas.order.api.dto.*;
import com.sportsaas.order.domain.Order;
import com.sportsaas.order.domain.OrderService;
import com.sportsaas.order.domain.OrderStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "Gestion des commandes")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;

    @PostMapping
    @Operation(summary = "Creer une commande")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
        Order order = orderMapper.toOrder(request);
        Order created = orderService.create(order);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderMapper.toOrderResponse(created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Recuperer une commande")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderResponse> getById(@PathVariable UUID id) {
        Order order = orderService.findById(id)
                .orElseThrow(() -> new NotFoundException("Order", id));
        return ResponseEntity.ok(orderMapper.toOrderResponse(order));
    }

    @GetMapping("/number/{orderNumber}")
    @Operation(summary = "Recuperer une commande par numero")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderResponse> getByNumber(@PathVariable String orderNumber) {
        Order order = orderService.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new NotFoundException("Order with number: " + orderNumber));
        return ResponseEntity.ok(orderMapper.toOrderResponse(order));
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Lister les commandes d'un client")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageResponse<OrderResponse>> getByCustomer(
            @PathVariable UUID customerId,
            Pageable pageable) {
        Page<Order> page = orderService.findByCustomerId(customerId, pageable);
        return ResponseEntity.ok(PageResponse.of(page, orderMapper::toOrderResponse));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Lister les commandes par statut")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<PageResponse<OrderResponse>> getByStatus(
            @PathVariable OrderStatus status,
            Pageable pageable) {
        Page<Order> page = orderService.findByStatus(status, pageable);
        return ResponseEntity.ok(PageResponse.of(page, orderMapper::toOrderResponse));
    }

    @PostMapping("/{id}/items")
    @Operation(summary = "Ajouter un article")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderResponse> addItem(
            @PathVariable UUID id,
            @Valid @RequestBody AddItemRequest request) {
        Order order = orderService.addItem(id, request.getProductId(), request.getQuantity());
        return ResponseEntity.ok(orderMapper.toOrderResponse(order));
    }

    @PutMapping("/{orderId}/items/{itemId}")
    @Operation(summary = "Modifier la quantite d'un article")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderResponse> updateItemQuantity(
            @PathVariable UUID orderId,
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateQuantityRequest request) {
        Order order = orderService.updateItemQuantity(orderId, itemId, request.getQuantity());
        return ResponseEntity.ok(orderMapper.toOrderResponse(order));
    }

    @DeleteMapping("/{orderId}/items/{itemId}")
    @Operation(summary = "Supprimer un article")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderResponse> removeItem(
            @PathVariable UUID orderId,
            @PathVariable UUID itemId) {
        Order order = orderService.removeItem(orderId, itemId);
        return ResponseEntity.ok(orderMapper.toOrderResponse(order));
    }

    @PostMapping("/{id}/submit")
    @Operation(summary = "Soumettre la commande")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderResponse> submit(@PathVariable UUID id) {
        Order order = orderService.submit(id);
        return ResponseEntity.ok(orderMapper.toOrderResponse(order));
    }

    @PostMapping("/{id}/pay")
    @Operation(summary = "Marquer comme payee")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<OrderResponse> markAsPaid(
            @PathVariable UUID id,
            @RequestParam(required = false) String paymentReference) {
        Order order = orderService.markAsPaid(id, paymentReference);
        return ResponseEntity.ok(orderMapper.toOrderResponse(order));
    }

    @PostMapping("/{id}/process")
    @Operation(summary = "Demarrer la preparation")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<OrderResponse> startProcessing(@PathVariable UUID id) {
        Order order = orderService.startProcessing(id);
        return ResponseEntity.ok(orderMapper.toOrderResponse(order));
    }

    @PostMapping("/{id}/ready")
    @Operation(summary = "Marquer comme prete")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<OrderResponse> markAsReady(@PathVariable UUID id) {
        Order order = orderService.markAsReady(id);
        return ResponseEntity.ok(orderMapper.toOrderResponse(order));
    }

    @PostMapping("/{id}/ship")
    @Operation(summary = "Expedier la commande")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<OrderResponse> ship(
            @PathVariable UUID id,
            @RequestParam(required = false) String trackingNumber) {
        Order order = orderService.ship(id, trackingNumber);
        return ResponseEntity.ok(orderMapper.toOrderResponse(order));
    }

    @PostMapping("/{id}/deliver")
    @Operation(summary = "Marquer comme livree")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<OrderResponse> deliver(@PathVariable UUID id) {
        Order order = orderService.deliver(id);
        return ResponseEntity.ok(orderMapper.toOrderResponse(order));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Annuler la commande")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderResponse> cancel(
            @PathVariable UUID id,
            @RequestParam String reason) {
        Order order = orderService.cancel(id, reason);
        return ResponseEntity.ok(orderMapper.toOrderResponse(order));
    }

    @PostMapping("/{id}/discount")
    @Operation(summary = "Appliquer une remise")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<OrderResponse> applyDiscount(
            @PathVariable UUID id,
            @Valid @RequestBody ApplyDiscountRequest request) {
        Order order = orderService.applyDiscount(id, request.getDiscountCode(), request.getAmount());
        return ResponseEntity.ok(orderMapper.toOrderResponse(order));
    }
}
