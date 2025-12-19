package com.sportsaas.order.domain;

import com.sportsaas.common.domain.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entite Order - Commande client.
 */
@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_orders_customer", columnList = "customer_id"),
    @Index(name = "idx_orders_status", columnList = "status"),
    @Index(name = "idx_orders_number", columnList = "order_number")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order extends TenantAwareEntity {

    @Column(name = "order_number", nullable = false, unique = true)
    private String orderNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderType type = OrderType.SALE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.DRAFT;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "customer_email")
    private String customerEmail;

    @Column(name = "customer_phone")
    private String customerPhone;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<OrderItem> items = new ArrayList<>();

    /** Sous-total (avant taxes et remises) */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    /** Montant de la remise */
    @Column(precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    /** Code de remise applique */
    @Column(name = "discount_code")
    private String discountCode;

    /** Montant des taxes */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    /** Frais de livraison */
    @Column(precision = 10, scale = 2)
    private BigDecimal shippingAmount = BigDecimal.ZERO;

    /** Total de la commande */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    /** Devise */
    @Column(nullable = false, length = 3)
    private String currency = "EUR";

    /** Adresse de livraison */
    @Column(name = "shipping_address", columnDefinition = "TEXT")
    private String shippingAddress;

    /** Adresse de facturation */
    @Column(name = "billing_address", columnDefinition = "TEXT")
    private String billingAddress;

    /** Notes internes */
    @Column(columnDefinition = "TEXT")
    private String notes;

    /** Notes du client */
    @Column(name = "customer_notes", columnDefinition = "TEXT")
    private String customerNotes;

    /** Date de paiement */
    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    /** Date de livraison/retrait */
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    /** Date d'annulation */
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    /** Raison d'annulation */
    @Column(name = "cancellation_reason")
    private String cancellationReason;

    /**
     * Ajoute un item a la commande.
     */
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
        recalculateTotals();
    }

    /**
     * Retire un item de la commande.
     */
    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
        recalculateTotals();
    }

    /**
     * Recalcule les totaux de la commande.
     */
    public void recalculateTotals() {
        this.subtotal = items.stream()
                .map(OrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.total = this.subtotal
                .subtract(this.discountAmount != null ? this.discountAmount : BigDecimal.ZERO)
                .add(this.taxAmount)
                .add(this.shippingAmount != null ? this.shippingAmount : BigDecimal.ZERO);
    }

    /**
     * Verifie si la commande peut etre annulee.
     */
    public boolean canCancel() {
        return status == OrderStatus.DRAFT ||
               status == OrderStatus.PENDING_PAYMENT ||
               status == OrderStatus.PAID ||
               status == OrderStatus.PROCESSING;
    }

    /**
     * Verifie si la commande est finalisee.
     */
    public boolean isCompleted() {
        return status == OrderStatus.DELIVERED ||
               status == OrderStatus.CANCELLED ||
               status == OrderStatus.REFUNDED;
    }
}
