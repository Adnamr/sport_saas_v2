package com.sportsaas.order.domain;

import com.sportsaas.catalog.domain.Product;
import com.sportsaas.common.domain.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Entite OrderItem - Ligne de commande.
 */
@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem extends TenantAwareEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** Nom du produit au moment de la commande */
    @Column(name = "product_name", nullable = false)
    private String productName;

    /** SKU du produit au moment de la commande */
    @Column(name = "product_sku", nullable = false)
    private String productSku;

    /** Quantite commandee */
    @Column(nullable = false)
    private int quantity;

    /** Prix unitaire au moment de la commande */
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    /** Remise sur la ligne */
    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    /** Total de la ligne (quantite * prix - remise) */
    @Column(name = "line_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal lineTotal = BigDecimal.ZERO;

    /** Notes sur cet item */
    @Column(columnDefinition = "TEXT")
    private String notes;

    /**
     * Recalcule le total de la ligne.
     */
    public void recalculateLineTotal() {
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
