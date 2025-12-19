package com.sportsaas.billing.domain;

import com.sportsaas.common.domain.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entite Payment - Paiement client.
 */
@Entity
@Table(name = "payments", indexes = {
    @Index(name = "idx_payments_invoice", columnList = "invoice_id"),
    @Index(name = "idx_payments_customer", columnList = "customer_id"),
    @Index(name = "idx_payments_status", columnList = "status"),
    @Index(name = "idx_payments_reference", columnList = "payment_reference")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment extends TenantAwareEntity {

    @Column(name = "payment_reference", nullable = false, unique = true)
    private String paymentReference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "customer_name")
    private String customerName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;

    /** Montant du paiement */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    /** Devise */
    @Column(nullable = false, length = 3)
    private String currency = "EUR";

    /** Reference de transaction externe (Stripe, PayPal, etc.) */
    @Column(name = "external_reference")
    private String externalReference;

    /** Description du paiement */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** Notes internes */
    @Column(columnDefinition = "TEXT")
    private String notes;

    /** Date de traitement */
    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    /** Date d'echec */
    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    /** Raison de l'echec */
    @Column(name = "failure_reason")
    private String failureReason;

    /** Date de remboursement */
    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    /** Montant rembourse */
    @Column(name = "refunded_amount", precision = 10, scale = 2)
    private BigDecimal refundedAmount = BigDecimal.ZERO;

    /**
     * Marque le paiement comme complete.
     */
    public void complete(String externalRef) {
        this.status = PaymentStatus.COMPLETED;
        this.externalReference = externalRef;
        this.processedAt = LocalDateTime.now();
    }

    /**
     * Marque le paiement comme echoue.
     */
    public void fail(String reason) {
        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
        this.failedAt = LocalDateTime.now();
    }

    /**
     * Effectue un remboursement.
     */
    public void refund(BigDecimal refundAmount) {
        this.refundedAmount = this.refundedAmount.add(refundAmount);
        if (this.refundedAmount.compareTo(this.amount) >= 0) {
            this.status = PaymentStatus.REFUNDED;
        }
        this.refundedAt = LocalDateTime.now();
    }

    /**
     * Verifie si le paiement peut etre rembourse.
     */
    public boolean canRefund() {
        return status == PaymentStatus.COMPLETED &&
               refundedAmount.compareTo(amount) < 0;
    }
}
