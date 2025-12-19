package com.sportsaas.inventory.domain;

import com.sportsaas.catalog.domain.Product;
import com.sportsaas.common.domain.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Entite StockMovement - Historique des mouvements de stock.
 */
@Entity
@Table(name = "stock_movements", indexes = {
    @Index(name = "idx_stock_movements_product", columnList = "product_id"),
    @Index(name = "idx_stock_movements_type", columnList = "movement_type"),
    @Index(name = "idx_stock_movements_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockMovement extends TenantAwareEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false)
    private MovementType type;

    /** Quantite du mouvement (positive) */
    @Column(nullable = false)
    private int quantity;

    /** Quantite avant le mouvement */
    @Column(nullable = false)
    private int quantityBefore;

    /** Quantite apres le mouvement */
    @Column(nullable = false)
    private int quantityAfter;

    /** Reference externe (ex: numero de commande, bon de livraison) */
    @Column
    private String reference;

    /** ID de la reservation associee (si applicable) */
    @Column(name = "reservation_id")
    private UUID reservationId;

    /** Raison du mouvement */
    @Column(columnDefinition = "TEXT")
    private String reason;

    /** ID de l'utilisateur ayant effectue le mouvement */
    @Column(name = "performed_by")
    private UUID performedBy;
}
