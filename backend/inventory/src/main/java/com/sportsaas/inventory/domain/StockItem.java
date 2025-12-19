package com.sportsaas.inventory.domain;

import com.sportsaas.catalog.domain.Product;
import com.sportsaas.common.domain.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entite StockItem - Niveau de stock pour un produit.
 */
@Entity
@Table(name = "stock_items", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tenant_id", "product_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockItem extends TenantAwareEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** Quantite disponible (physique - reservee) */
    @Column(nullable = false)
    private int availableQuantity = 0;

    /** Quantite reservee pour des commandes en cours */
    @Column(nullable = false)
    private int reservedQuantity = 0;

    /** Quantite physique totale en stock */
    @Column(nullable = false)
    private int physicalQuantity = 0;

    /** Seuil d'alerte pour stock bas */
    @Column(nullable = false)
    private int lowStockThreshold = 5;

    /** Emplacement dans l'entrepot */
    @Column
    private String location;

    /**
     * Verifie si le stock est bas.
     */
    public boolean isLowStock() {
        return availableQuantity <= lowStockThreshold;
    }

    /**
     * Verifie si le produit est en rupture.
     */
    public boolean isOutOfStock() {
        return availableQuantity <= 0;
    }

    /**
     * Recalcule la quantite disponible.
     */
    public void recalculateAvailable() {
        this.availableQuantity = this.physicalQuantity - this.reservedQuantity;
    }

    /**
     * Ajoute du stock physique.
     */
    public void addStock(int quantity) {
        this.physicalQuantity += quantity;
        recalculateAvailable();
    }

    /**
     * Retire du stock physique.
     */
    public void removeStock(int quantity) {
        if (quantity > this.physicalQuantity) {
            throw new IllegalArgumentException("Quantite insuffisante en stock physique");
        }
        this.physicalQuantity -= quantity;
        recalculateAvailable();
    }

    /**
     * Reserve du stock.
     */
    public void reserve(int quantity) {
        if (quantity > this.availableQuantity) {
            throw new IllegalArgumentException("Quantite insuffisante disponible pour reservation");
        }
        this.reservedQuantity += quantity;
        recalculateAvailable();
    }

    /**
     * Libere du stock reserve.
     */
    public void release(int quantity) {
        if (quantity > this.reservedQuantity) {
            throw new IllegalArgumentException("Quantite a liberer superieure a la quantite reservee");
        }
        this.reservedQuantity -= quantity;
        recalculateAvailable();
    }
}
