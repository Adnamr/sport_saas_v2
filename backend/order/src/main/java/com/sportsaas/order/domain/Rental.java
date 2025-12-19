package com.sportsaas.order.domain;

import com.sportsaas.catalog.domain.Product;
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
 * Entite Rental - Location d'equipement sportif.
 */
@Entity
@Table(name = "rentals", indexes = {
    @Index(name = "idx_rentals_customer", columnList = "customer_id"),
    @Index(name = "idx_rentals_product", columnList = "product_id"),
    @Index(name = "idx_rentals_status", columnList = "status"),
    @Index(name = "idx_rentals_dates", columnList = "start_date, end_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Rental extends TenantAwareEntity {

    @Column(name = "rental_number", nullable = false, unique = true)
    private String rentalNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** Nom du produit au moment de la location */
    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "customer_email")
    private String customerEmail;

    @Column(name = "customer_phone")
    private String customerPhone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RentalStatus status = RentalStatus.PENDING;

    /** Quantite louee */
    @Column(nullable = false)
    private int quantity = 1;

    /** Date de debut de location */
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    /** Date de fin prevue */
    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    /** Date de retour effective */
    @Column(name = "actual_return_date")
    private LocalDateTime actualReturnDate;

    /** Prix par jour */
    @Column(name = "daily_rate", nullable = false, precision = 10, scale = 2)
    private BigDecimal dailyRate;

    /** Nombre de jours */
    @Column(name = "rental_days", nullable = false)
    private int rentalDays;

    /** Montant de la caution */
    @Column(precision = 10, scale = 2)
    private BigDecimal deposit = BigDecimal.ZERO;

    /** Sous-total (jours * tarif journalier) */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    /** Frais supplementaires (retard, dommages) */
    @Column(name = "extra_charges", precision = 10, scale = 2)
    private BigDecimal extraCharges = BigDecimal.ZERO;

    /** Description des frais supplementaires */
    @Column(name = "extra_charges_notes", columnDefinition = "TEXT")
    private String extraChargesNotes;

    /** Total */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    /** Devise */
    @Column(nullable = false, length = 3)
    private String currency = "EUR";

    /** Notes */
    @Column(columnDefinition = "TEXT")
    private String notes;

    /** Etat du materiel au depart */
    @Column(name = "condition_at_start", columnDefinition = "TEXT")
    private String conditionAtStart;

    /** Etat du materiel au retour */
    @Column(name = "condition_at_return", columnDefinition = "TEXT")
    private String conditionAtReturn;

    /**
     * Calcule le nombre de jours entre les dates.
     */
    public int calculateRentalDays() {
        if (startDate == null || endDate == null) return 0;
        long days = java.time.Duration.between(startDate, endDate).toDays();
        return Math.max(1, (int) days);
    }

    /**
     * Recalcule les totaux.
     */
    public void recalculateTotals() {
        this.rentalDays = calculateRentalDays();
        this.subtotal = this.dailyRate.multiply(BigDecimal.valueOf(this.rentalDays))
                .multiply(BigDecimal.valueOf(this.quantity));
        this.total = this.subtotal.add(this.extraCharges != null ? this.extraCharges : BigDecimal.ZERO);
    }

    /**
     * Verifie si la location est en retard.
     */
    public boolean isOverdue() {
        return status == RentalStatus.ACTIVE &&
               LocalDateTime.now().isAfter(endDate);
    }

    /**
     * Verifie si la location peut etre annulee.
     */
    public boolean canCancel() {
        return status == RentalStatus.PENDING || status == RentalStatus.CONFIRMED;
    }

    /**
     * Verifie si la location peut demarrer.
     */
    public boolean canStart() {
        return status == RentalStatus.CONFIRMED;
    }

    /**
     * Verifie si la location peut etre retournee.
     */
    public boolean canReturn() {
        return status == RentalStatus.ACTIVE || status == RentalStatus.OVERDUE;
    }
}
