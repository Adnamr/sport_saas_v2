package com.sportsaas.order.api;

import com.sportsaas.catalog.domain.Product;
import com.sportsaas.catalog.domain.ProductRepository;
import com.sportsaas.common.dto.PageResponse;
import com.sportsaas.common.exception.NotFoundException;
import com.sportsaas.order.api.dto.*;
import com.sportsaas.order.domain.Rental;
import com.sportsaas.order.domain.RentalService;
import com.sportsaas.order.domain.RentalStatus;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rentals")
@Tag(name = "Rentals", description = "Gestion des locations")
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;

    @PostMapping
    @Operation(summary = "Creer une location")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RentalResponse> create(@Valid @RequestBody CreateRentalRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new NotFoundException("Product", request.getProductId()));

        Rental rental = orderMapper.toRental(request);
        rental.setProduct(product);

        Rental created = rentalService.create(rental);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderMapper.toRentalResponse(created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Recuperer une location")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RentalResponse> getById(@PathVariable UUID id) {
        Rental rental = rentalService.findById(id)
                .orElseThrow(() -> new NotFoundException("Rental", id));
        return ResponseEntity.ok(orderMapper.toRentalResponse(rental));
    }

    @GetMapping("/number/{rentalNumber}")
    @Operation(summary = "Recuperer une location par numero")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RentalResponse> getByNumber(@PathVariable String rentalNumber) {
        Rental rental = rentalService.findByRentalNumber(rentalNumber)
                .orElseThrow(() -> new NotFoundException("Rental with number: " + rentalNumber));
        return ResponseEntity.ok(orderMapper.toRentalResponse(rental));
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Lister les locations d'un client")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageResponse<RentalResponse>> getByCustomer(
            @PathVariable UUID customerId,
            Pageable pageable) {
        Page<Rental> page = rentalService.findByCustomerId(customerId, pageable);
        return ResponseEntity.ok(PageResponse.of(page, orderMapper::toRentalResponse));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Lister les locations par statut")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<PageResponse<RentalResponse>> getByStatus(
            @PathVariable RentalStatus status,
            Pageable pageable) {
        Page<Rental> page = rentalService.findByStatus(status, pageable);
        return ResponseEntity.ok(PageResponse.of(page, orderMapper::toRentalResponse));
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Lister les locations d'un produit")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<PageResponse<RentalResponse>> getByProduct(
            @PathVariable UUID productId,
            Pageable pageable) {
        Page<Rental> page = rentalService.findByProductId(productId, pageable);
        return ResponseEntity.ok(PageResponse.of(page, orderMapper::toRentalResponse));
    }

    @GetMapping("/overdue")
    @Operation(summary = "Lister les locations en retard")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<RentalResponse>> getOverdue() {
        List<Rental> rentals = rentalService.findOverdue();
        return ResponseEntity.ok(orderMapper.toRentalResponseList(rentals));
    }

    @PostMapping("/{id}/confirm")
    @Operation(summary = "Confirmer une location")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<RentalResponse> confirm(@PathVariable UUID id) {
        Rental rental = rentalService.confirm(id);
        return ResponseEntity.ok(orderMapper.toRentalResponse(rental));
    }

    @PostMapping("/{id}/start")
    @Operation(summary = "Demarrer une location (depart materiel)")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<RentalResponse> start(
            @PathVariable UUID id,
            @RequestParam(required = false) String conditionAtStart) {
        Rental rental = rentalService.start(id, conditionAtStart);
        return ResponseEntity.ok(orderMapper.toRentalResponse(rental));
    }

    @PostMapping("/{id}/return")
    @Operation(summary = "Retourner une location")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<RentalResponse> returnRental(
            @PathVariable UUID id,
            @RequestParam(required = false) String conditionAtReturn) {
        Rental rental = rentalService.returnRental(id, conditionAtReturn);
        return ResponseEntity.ok(orderMapper.toRentalResponse(rental));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Annuler une location")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RentalResponse> cancel(
            @PathVariable UUID id,
            @RequestParam String reason) {
        Rental rental = rentalService.cancel(id, reason);
        return ResponseEntity.ok(orderMapper.toRentalResponse(rental));
    }

    @PostMapping("/{id}/extend")
    @Operation(summary = "Prolonger une location")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RentalResponse> extend(
            @PathVariable UUID id,
            @Valid @RequestBody ExtendRentalRequest request) {
        Rental rental = rentalService.extend(id, request.getNewEndDate());
        return ResponseEntity.ok(orderMapper.toRentalResponse(rental));
    }

    @PostMapping("/{id}/extra-charges")
    @Operation(summary = "Ajouter des frais supplementaires")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<RentalResponse> addExtraCharges(
            @PathVariable UUID id,
            @Valid @RequestBody AddExtraChargesRequest request) {
        Rental rental = rentalService.addExtraCharges(id, request.getAmount(), request.getReason());
        return ResponseEntity.ok(orderMapper.toRentalResponse(rental));
    }

    @PostMapping("/process-overdue")
    @Operation(summary = "Traiter les locations en retard")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<Integer> processOverdue() {
        int count = rentalService.processOverdueRentals();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/check-availability")
    @Operation(summary = "Verifier la disponibilite d'un produit")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Boolean> checkAvailability(
            @RequestParam UUID productId,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "1") int quantity) {
        java.time.LocalDateTime start = java.time.LocalDateTime.parse(startDate);
        java.time.LocalDateTime end = java.time.LocalDateTime.parse(endDate);
        boolean available = rentalService.isProductAvailable(productId, start, end, quantity);
        return ResponseEntity.ok(available);
    }
}
