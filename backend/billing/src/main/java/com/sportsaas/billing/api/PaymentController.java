package com.sportsaas.billing.api;

import com.sportsaas.billing.api.dto.*;
import com.sportsaas.billing.domain.Payment;
import com.sportsaas.billing.domain.PaymentService;
import com.sportsaas.billing.domain.PaymentStatus;
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
@RequestMapping("/api/payments")
@Tag(name = "Payments", description = "Gestion des paiements")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final BillingMapper billingMapper;

    @PostMapping
    @Operation(summary = "Creer un paiement")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<PaymentResponse> create(@Valid @RequestBody CreatePaymentRequest request) {
        Payment created = paymentService.createForInvoice(
                request.getInvoiceId(),
                request.getAmount(),
                request.getMethod());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(billingMapper.toPaymentResponse(created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Recuperer un paiement")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaymentResponse> getById(@PathVariable UUID id) {
        Payment payment = paymentService.findById(id)
                .orElseThrow(() -> new NotFoundException("Payment", id));
        return ResponseEntity.ok(billingMapper.toPaymentResponse(payment));
    }

    @GetMapping("/reference/{reference}")
    @Operation(summary = "Recuperer un paiement par reference")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaymentResponse> getByReference(@PathVariable String reference) {
        Payment payment = paymentService.findByPaymentReference(reference)
                .orElseThrow(() -> new NotFoundException("Payment with reference: " + reference));
        return ResponseEntity.ok(billingMapper.toPaymentResponse(payment));
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Lister les paiements d'un client")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageResponse<PaymentResponse>> getByCustomer(
            @PathVariable UUID customerId,
            Pageable pageable) {
        Page<Payment> page = paymentService.findByCustomerId(customerId, pageable);
        return ResponseEntity.ok(PageResponse.of(page, billingMapper::toPaymentResponse));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Lister les paiements par statut")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<PageResponse<PaymentResponse>> getByStatus(
            @PathVariable PaymentStatus status,
            Pageable pageable) {
        Page<Payment> page = paymentService.findByStatus(status, pageable);
        return ResponseEntity.ok(PageResponse.of(page, billingMapper::toPaymentResponse));
    }

    @GetMapping("/invoice/{invoiceId}")
    @Operation(summary = "Lister les paiements d'une facture")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PaymentResponse>> getByInvoice(@PathVariable UUID invoiceId) {
        List<Payment> payments = paymentService.findByInvoiceId(invoiceId);
        return ResponseEntity.ok(billingMapper.toPaymentResponseList(payments));
    }

    @PostMapping("/{id}/process")
    @Operation(summary = "Demarrer le traitement du paiement")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<PaymentResponse> process(@PathVariable UUID id) {
        Payment payment = paymentService.process(id);
        return ResponseEntity.ok(billingMapper.toPaymentResponse(payment));
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "Marquer le paiement comme complete")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<PaymentResponse> complete(
            @PathVariable UUID id,
            @RequestParam(required = false) String externalReference) {
        Payment payment = paymentService.complete(id, externalReference);
        return ResponseEntity.ok(billingMapper.toPaymentResponse(payment));
    }

    @PostMapping("/{id}/fail")
    @Operation(summary = "Marquer le paiement comme echoue")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<PaymentResponse> fail(
            @PathVariable UUID id,
            @RequestParam String reason) {
        Payment payment = paymentService.fail(id, reason);
        return ResponseEntity.ok(billingMapper.toPaymentResponse(payment));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Annuler le paiement")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<PaymentResponse> cancel(@PathVariable UUID id) {
        Payment payment = paymentService.cancel(id);
        return ResponseEntity.ok(billingMapper.toPaymentResponse(payment));
    }

    @PostMapping("/{id}/refund")
    @Operation(summary = "Rembourser le paiement")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<PaymentResponse> refund(
            @PathVariable UUID id,
            @Valid @RequestBody RefundRequest request) {
        Payment payment = paymentService.refund(id, request.getAmount(), request.getReason());
        return ResponseEntity.ok(billingMapper.toPaymentResponse(payment));
    }

    @GetMapping("/customer/{customerId}/stats")
    @Operation(summary = "Statistiques de paiement d'un client")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'EMPLOYEE')")
    public ResponseEntity<PaymentService.CustomerPaymentStats> getCustomerStats(
            @PathVariable UUID customerId) {
        return ResponseEntity.ok(paymentService.getCustomerStats(customerId));
    }
}
