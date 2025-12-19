package com.sportsaas.billing.domain;

import com.sportsaas.common.domain.TenantAwareEntity;
import com.sportsaas.order.domain.Order;
import com.sportsaas.order.domain.Rental;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entite Invoice - Facture client.
 */
@Entity
@Table(name = "invoices", indexes = {
    @Index(name = "idx_invoices_customer", columnList = "customer_id"),
    @Index(name = "idx_invoices_status", columnList = "status"),
    @Index(name = "idx_invoices_number", columnList = "invoice_number"),
    @Index(name = "idx_invoices_due_date", columnList = "due_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Invoice extends TenantAwareEntity {

    @Column(name = "invoice_number", nullable = false, unique = true)
    private String invoiceNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rental_id")
    private Rental rental;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "customer_email")
    private String customerEmail;

    @Column(name = "customer_phone")
    private String customerPhone;

    @Column(name = "customer_address", columnDefinition = "TEXT")
    private String customerAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvoiceStatus status = InvoiceStatus.DRAFT;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<InvoiceItem> items = new ArrayList<>();

    /** Date d'emission */
    @Column(name = "issue_date")
    private LocalDate issueDate;

    /** Date d'echeance */
    @Column(name = "due_date")
    private LocalDate dueDate;

    /** Sous-total HT */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    /** Taux de TVA (en %) */
    @Column(name = "tax_rate", precision = 5, scale = 2)
    private BigDecimal taxRate = BigDecimal.ZERO;

    /** Montant de la TVA */
    @Column(name = "tax_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    /** Remise globale */
    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    /** Total TTC */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    /** Montant deja paye */
    @Column(name = "paid_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    /** Solde restant a payer */
    @Column(name = "balance_due", nullable = false, precision = 10, scale = 2)
    private BigDecimal balanceDue = BigDecimal.ZERO;

    /** Devise */
    @Column(nullable = false, length = 3)
    private String currency = "EUR";

    /** Notes internes */
    @Column(columnDefinition = "TEXT")
    private String notes;

    /** Conditions de paiement */
    @Column(name = "payment_terms", columnDefinition = "TEXT")
    private String paymentTerms;

    /** Date d'envoi */
    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    /** Date de paiement complet */
    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    /** Date d'annulation */
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    /** Raison d'annulation */
    @Column(name = "cancellation_reason")
    private String cancellationReason;

    /**
     * Ajoute un item a la facture.
     */
    public void addItem(InvoiceItem item) {
        items.add(item);
        item.setInvoice(this);
        recalculateTotals();
    }

    /**
     * Retire un item de la facture.
     */
    public void removeItem(InvoiceItem item) {
        items.remove(item);
        item.setInvoice(null);
        recalculateTotals();
    }

    /**
     * Recalcule les totaux de la facture.
     */
    public void recalculateTotals() {
        this.subtotal = items.stream()
                .map(InvoiceItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal afterDiscount = this.subtotal.subtract(
                this.discountAmount != null ? this.discountAmount : BigDecimal.ZERO);

        if (this.taxRate != null && this.taxRate.compareTo(BigDecimal.ZERO) > 0) {
            this.taxAmount = afterDiscount.multiply(this.taxRate).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
        } else {
            this.taxAmount = BigDecimal.ZERO;
        }

        this.total = afterDiscount.add(this.taxAmount);
        this.balanceDue = this.total.subtract(this.paidAmount);
    }

    /**
     * Enregistre un paiement.
     */
    public void recordPayment(BigDecimal amount) {
        this.paidAmount = this.paidAmount.add(amount);
        this.balanceDue = this.total.subtract(this.paidAmount);

        if (this.balanceDue.compareTo(BigDecimal.ZERO) <= 0) {
            this.status = InvoiceStatus.PAID;
            this.paidAt = LocalDateTime.now();
            this.balanceDue = BigDecimal.ZERO;
        } else if (this.paidAmount.compareTo(BigDecimal.ZERO) > 0) {
            this.status = InvoiceStatus.PARTIALLY_PAID;
        }
    }

    /**
     * Verifie si la facture est en retard.
     */
    public boolean isOverdue() {
        return dueDate != null &&
               LocalDate.now().isAfter(dueDate) &&
               status != InvoiceStatus.PAID &&
               status != InvoiceStatus.CANCELLED &&
               status != InvoiceStatus.REFUNDED;
    }

    /**
     * Verifie si la facture peut etre modifiee.
     */
    public boolean canModify() {
        return status == InvoiceStatus.DRAFT;
    }

    /**
     * Verifie si la facture peut etre annulee.
     */
    public boolean canCancel() {
        return status == InvoiceStatus.DRAFT ||
               status == InvoiceStatus.FINALIZED ||
               status == InvoiceStatus.SENT;
    }
}
