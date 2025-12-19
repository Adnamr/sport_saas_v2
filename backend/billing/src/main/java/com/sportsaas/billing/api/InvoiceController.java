package com.sportsaas.billing.api;

import com.sportsaas.billing.api.dto.*;
import com.sportsaas.billing.domain.Invoice;
import com.sportsaas.billing.domain.InvoiceService;
import com.sportsaas.billing.domain.InvoiceStatus;
import com.sportsaas.common.dto.PageResponse;
import com.sportsaas.common.exception.NotFoundException;
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
@RequestMapping("/api/invoices")
@Tag(name = "Invoices", description = "Gestion des factures")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final BillingMapper billingMapper;

    @PostMapping
    @Operation(summary = "Creer une facture")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<InvoiceResponse> create(@Valid @RequestBody CreateInvoiceRequest request) {
        Invoice invoice = billingMapper.toInvoice(request);
        Invoice created = invoiceService.create(invoice);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(billingMapper.toInvoiceResponse(created));
    }

    @PostMapping("/from-order/{orderId}")
    @Operation(summary = "Creer une facture a partir d'une commande")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<InvoiceResponse> createFromOrder(@PathVariable UUID orderId) {
        Invoice created = invoiceService.createFromOrder(orderId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(billingMapper.toInvoiceResponse(created));
    }

    @PostMapping("/from-rental/{rentalId}")
    @Operation(summary = "Creer une facture a partir d'une location")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<InvoiceResponse> createFromRental(@PathVariable UUID rentalId) {
        Invoice created = invoiceService.createFromRental(rentalId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(billingMapper.toInvoiceResponse(created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Recuperer une facture")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<InvoiceResponse> getById(@PathVariable UUID id) {
        Invoice invoice = invoiceService.findById(id)
                .orElseThrow(() -> new NotFoundException("Invoice", id));
        return ResponseEntity.ok(billingMapper.toInvoiceResponse(invoice));
    }

    @GetMapping("/number/{invoiceNumber}")
    @Operation(summary = "Recuperer une facture par numero")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<InvoiceResponse> getByNumber(@PathVariable String invoiceNumber) {
        Invoice invoice = invoiceService.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new NotFoundException("Invoice with number: " + invoiceNumber));
        return ResponseEntity.ok(billingMapper.toInvoiceResponse(invoice));
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Lister les factures d'un client")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageResponse<InvoiceResponse>> getByCustomer(
            @PathVariable UUID customerId,
            Pageable pageable) {
        Page<Invoice> page = invoiceService.findByCustomerId(customerId, pageable);
        return ResponseEntity.ok(PageResponse.of(page, billingMapper::toInvoiceResponse));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Lister les factures par statut")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<PageResponse<InvoiceResponse>> getByStatus(
            @PathVariable InvoiceStatus status,
            Pageable pageable) {
        Page<Invoice> page = invoiceService.findByStatus(status, pageable);
        return ResponseEntity.ok(PageResponse.of(page, billingMapper::toInvoiceResponse));
    }

    @GetMapping("/overdue")
    @Operation(summary = "Lister les factures en retard")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<InvoiceResponse>> getOverdue() {
        List<Invoice> invoices = invoiceService.findOverdue();
        return ResponseEntity.ok(billingMapper.toInvoiceResponseList(invoices));
    }

    @PostMapping("/{id}/items")
    @Operation(summary = "Ajouter un article")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<InvoiceResponse> addItem(
            @PathVariable UUID id,
            @Valid @RequestBody AddInvoiceItemRequest request) {
        Invoice invoice = invoiceService.addItem(id, request.getDescription(),
                request.getQuantity(), request.getUnitPrice());
        return ResponseEntity.ok(billingMapper.toInvoiceResponse(invoice));
    }

    @PutMapping("/{invoiceId}/items/{itemId}")
    @Operation(summary = "Modifier un article")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<InvoiceResponse> updateItem(
            @PathVariable UUID invoiceId,
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateInvoiceItemRequest request) {
        Invoice invoice = invoiceService.updateItem(invoiceId, itemId,
                request.getQuantity(), request.getUnitPrice());
        return ResponseEntity.ok(billingMapper.toInvoiceResponse(invoice));
    }

    @DeleteMapping("/{invoiceId}/items/{itemId}")
    @Operation(summary = "Supprimer un article")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<InvoiceResponse> removeItem(
            @PathVariable UUID invoiceId,
            @PathVariable UUID itemId) {
        Invoice invoice = invoiceService.removeItem(invoiceId, itemId);
        return ResponseEntity.ok(billingMapper.toInvoiceResponse(invoice));
    }

    @PostMapping("/{id}/finalize")
    @Operation(summary = "Finaliser la facture")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<InvoiceResponse> finalize(@PathVariable UUID id) {
        Invoice invoice = invoiceService.finalize(id);
        return ResponseEntity.ok(billingMapper.toInvoiceResponse(invoice));
    }

    @PostMapping("/{id}/send")
    @Operation(summary = "Envoyer la facture")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<InvoiceResponse> send(@PathVariable UUID id) {
        Invoice invoice = invoiceService.send(id);
        return ResponseEntity.ok(billingMapper.toInvoiceResponse(invoice));
    }

    @PostMapping("/{id}/discount")
    @Operation(summary = "Appliquer une remise")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<InvoiceResponse> applyDiscount(
            @PathVariable UUID id,
            @Valid @RequestBody ApplyDiscountRequest request) {
        Invoice invoice = invoiceService.applyDiscount(id, request.getAmount());
        return ResponseEntity.ok(billingMapper.toInvoiceResponse(invoice));
    }

    @PostMapping("/{id}/tax")
    @Operation(summary = "Definir le taux de TVA")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<InvoiceResponse> setTaxRate(
            @PathVariable UUID id,
            @Valid @RequestBody ApplyTaxRequest request) {
        Invoice invoice = invoiceService.setTaxRate(id, request.getTaxRate());
        return ResponseEntity.ok(billingMapper.toInvoiceResponse(invoice));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Annuler la facture")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<InvoiceResponse> cancel(
            @PathVariable UUID id,
            @RequestParam String reason) {
        Invoice invoice = invoiceService.cancel(id, reason);
        return ResponseEntity.ok(billingMapper.toInvoiceResponse(invoice));
    }

    @PostMapping("/process-overdue")
    @Operation(summary = "Traiter les factures en retard")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<Integer> processOverdue() {
        int count = invoiceService.processOverdueInvoices();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/customer/{customerId}/stats")
    @Operation(summary = "Statistiques de facturation d'un client")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<InvoiceService.CustomerBillingStats> getCustomerStats(
            @PathVariable UUID customerId) {
        return ResponseEntity.ok(invoiceService.getCustomerStats(customerId));
    }
}
