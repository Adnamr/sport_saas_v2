package com.sportsaas.inventory.domain;

import com.sportsaas.catalog.domain.Product;
import com.sportsaas.common.domain.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entite Reservation - Reservation temporaire de stock.
 */
@Entity
@Table(name = "reservations", indexes = {
    @Index(name = "idx_reservations_product", columnList = "product_id"),
    @Index(name = "idx_reservations_status", columnList = "status"),
    @Index(name = "idx_reservations_expires", columnList = "expires_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Reservation extends TenantAwareEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** Quantite reservee */
    @Column(nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status = ReservationStatus.ACTIVE;

    /** Date d'expiration de la reservation */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /** Reference externe (ex: ID de commande, ID de panier) */
    @Column
    private String reference;

    /** ID du client ayant fait la reservation */
    @Column(name = "customer_id")
    private UUID customerId;

    /** Note ou commentaire */
    @Column(columnDefinition = "TEXT")
    private String notes;

    /**
     * Verifie si la reservation est expiree.
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt) && status == ReservationStatus.ACTIVE;
    }

    /**
     * Verifie si la reservation peut etre confirmee.
     */
    public boolean canConfirm() {
        return status == ReservationStatus.ACTIVE && !isExpired();
    }

    /**
     * Verifie si la reservation peut etre annulee.
     */
    public boolean canCancel() {
        return status == ReservationStatus.ACTIVE || status == ReservationStatus.CONFIRMED;
    }
}
