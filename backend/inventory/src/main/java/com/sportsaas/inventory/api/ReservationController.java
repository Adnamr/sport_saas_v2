package com.sportsaas.inventory.api;

import com.sportsaas.common.dto.PageResponse;
import com.sportsaas.common.exception.NotFoundException;
import com.sportsaas.inventory.api.dto.*;
import com.sportsaas.inventory.domain.Reservation;
import com.sportsaas.inventory.domain.ReservationService;
import com.sportsaas.inventory.domain.ReservationStatus;
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

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reservations")
@Tag(name = "Reservations", description = "Gestion des reservations de stock")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final InventoryMapper inventoryMapper;

    @PostMapping
    @Operation(summary = "Creer une reservation")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReservationResponse> create(@Valid @RequestBody CreateReservationRequest request) {
        Reservation reservation = reservationService.create(
                request.getProductId(),
                request.getQuantity(),
                Duration.ofMinutes(request.getDurationMinutes()),
                request.getReference(),
                request.getCustomerId()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inventoryMapper.toReservationResponse(reservation));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Recuperer une reservation")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReservationResponse> getById(@PathVariable UUID id) {
        Reservation reservation = reservationService.findById(id)
                .orElseThrow(() -> new NotFoundException("Reservation", id));
        return ResponseEntity.ok(inventoryMapper.toReservationResponse(reservation));
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Lister les reservations d'un produit")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<PageResponse<ReservationResponse>> getByProduct(
            @PathVariable UUID productId,
            Pageable pageable) {
        Page<Reservation> page = reservationService.findByProductId(productId, pageable);
        return ResponseEntity.ok(PageResponse.of(page, inventoryMapper::toReservationResponse));
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Lister les reservations d'un client")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ReservationResponse>> getByCustomer(@PathVariable UUID customerId) {
        List<Reservation> reservations = reservationService.findByCustomerId(customerId);
        return ResponseEntity.ok(inventoryMapper.toReservationResponseList(reservations));
    }

    @GetMapping("/customer/{customerId}/active")
    @Operation(summary = "Lister les reservations actives d'un client")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ReservationResponse>> getActiveByCustomer(@PathVariable UUID customerId) {
        List<Reservation> reservations = reservationService.findActiveByCustomerId(customerId);
        return ResponseEntity.ok(inventoryMapper.toReservationResponseList(reservations));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Lister les reservations par statut")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<PageResponse<ReservationResponse>> getByStatus(
            @PathVariable ReservationStatus status,
            Pageable pageable) {
        Page<Reservation> page = reservationService.findByStatus(status, pageable);
        return ResponseEntity.ok(PageResponse.of(page, inventoryMapper::toReservationResponse));
    }

    @PostMapping("/{id}/confirm")
    @Operation(summary = "Confirmer une reservation")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<ReservationResponse> confirm(@PathVariable UUID id) {
        Reservation reservation = reservationService.confirm(id);
        return ResponseEntity.ok(inventoryMapper.toReservationResponse(reservation));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Annuler une reservation")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReservationResponse> cancel(@PathVariable UUID id) {
        Reservation reservation = reservationService.cancel(id);
        return ResponseEntity.ok(inventoryMapper.toReservationResponse(reservation));
    }

    @PostMapping("/{id}/extend")
    @Operation(summary = "Prolonger une reservation")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReservationResponse> extend(
            @PathVariable UUID id,
            @Valid @RequestBody ExtendReservationRequest request) {
        Reservation reservation = reservationService.extend(id, Duration.ofMinutes(request.getAdditionalMinutes()));
        return ResponseEntity.ok(inventoryMapper.toReservationResponse(reservation));
    }

    @PostMapping("/{id}/release")
    @Operation(summary = "Liberer une reservation confirmee")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<ReservationResponse> release(@PathVariable UUID id) {
        Reservation reservation = reservationService.release(id);
        return ResponseEntity.ok(inventoryMapper.toReservationResponse(reservation));
    }

    @PostMapping("/process-expired")
    @Operation(summary = "Traiter les reservations expirees")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<Integer> processExpired() {
        int count = reservationService.processExpiredReservations();
        return ResponseEntity.ok(count);
    }
}
