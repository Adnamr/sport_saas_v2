package com.sportsaas.billing.domain;

import com.sportsaas.common.domain.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Entite InvoiceItem - Ligne de facture.
 */
@Entity
@Table(name = "invoice_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceItem extends TenantAwareEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    /** Description de l'article */
    @Column(nullable = false)
    private String description;

    /** Quantite */
    @Column(nullable = false)
    private int quantity = 1;

    /** Prix unitaire HT */
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice = BigDecimal.ZERO;

    /** Remise sur la ligne */
    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    /** Total de la ligne HT */
    @Column(name = "line_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal lineTotal = BigDecimal.ZERO;

    /** Notes sur cet item */
    @Column(columnDefinition = "TEXT")
    private String notes;

    /**
     * Recalcule le total de la ligne.
     */
    public void recalculateLineTotal() {
        if (this.unitPrice == null) {
            this.lineTotal = BigDecimal.ZERO;
            return;
        }
        BigDecimal gross = this.unitPrice.multiply(BigDecimal.valueOf(this.quantity));
        this.lineTotal = gross.subtract(this.discountAmount != null ? this.discountAmount : BigDecimal.ZERO);
    }

    /**
     * Definit la quantite et recalcule le total.
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
        recalculateLineTotal();
    }

    /**
     * Definit le prix unitaire et recalcule le total.
     */
    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        recalculateLineTotal();
    }
}
