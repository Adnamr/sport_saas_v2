package com.sportsaas.admin.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entite TenantInfo - Informations etendues sur un tenant.
 * Complement de Tenant pour la gestion admin.
 */
@Entity
@Table(name = "tenant_infos", indexes = {
    @Index(name = "idx_tenant_infos_status", columnList = "status"),
    @Index(name = "idx_tenant_infos_plan", columnList = "subscription_plan")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TenantInfo {

    @Id
    private UUID id;

    /** Nom commercial du tenant */
    @Column(name = "company_name", nullable = false)
    private String companyName;

    /** Email de contact principal */
    @Column(name = "contact_email", nullable = false)
    private String contactEmail;

    /** Telephone de contact */
    @Column(name = "contact_phone")
    private String contactPhone;

    /** Adresse */
    @Column(columnDefinition = "TEXT")
    private String address;

    /** Statut du tenant */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TenantStatus status = TenantStatus.PENDING_ACTIVATION;

    /** Plan d'abonnement */
    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_plan", nullable = false)
    private SubscriptionPlan subscriptionPlan = SubscriptionPlan.FREE;

    /** Date de debut d'abonnement */
    @Column(name = "subscription_start")
    private LocalDate subscriptionStart;

    /** Date de fin d'abonnement */
    @Column(name = "subscription_end")
    private LocalDate subscriptionEnd;

    /** Limite d'utilisateurs */
    @Column(name = "max_users")
    private Integer maxUsers = 5;

    /** Limite de produits */
    @Column(name = "max_products")
    private Integer maxProducts = 100;

    /** Stockage maximum (Mo) */
    @Column(name = "max_storage_mb")
    private Integer maxStorageMb = 500;

    /** Stockage utilise (Mo) */
    @Column(name = "used_storage_mb")
    private Integer usedStorageMb = 0;

    /** Notes internes (admin) */
    @Column(columnDefinition = "TEXT")
    private String notes;

    /** Date d'activation */
    @Column(name = "activated_at")
    private LocalDateTime activatedAt;

    /** Date de suspension */
    @Column(name = "suspended_at")
    private LocalDateTime suspendedAt;

    /** Raison de suspension */
    @Column(name = "suspension_reason")
    private String suspensionReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Active le tenant.
     */
    public void activate() {
        this.status = TenantStatus.ACTIVE;
        this.activatedAt = LocalDateTime.now();
        this.suspendedAt = null;
        this.suspensionReason = null;
    }

    /**
     * Suspend le tenant.
     */
    public void suspend(String reason) {
        this.status = TenantStatus.SUSPENDED;
        this.suspendedAt = LocalDateTime.now();
        this.suspensionReason = reason;
    }

    /**
     * Desactive le tenant.
     */
    public void deactivate() {
        this.status = TenantStatus.INACTIVE;
    }

    /**
     * Verifie si le tenant peut ajouter des utilisateurs.
     */
    public boolean canAddUsers(int currentCount) {
        return maxUsers == null || currentCount < maxUsers;
    }

    /**
     * Verifie si le tenant peut ajouter des produits.
     */
    public boolean canAddProducts(int currentCount) {
        return maxProducts == null || currentCount < maxProducts;
    }

    /**
     * Verifie si l'abonnement est expire.
     */
    public boolean isSubscriptionExpired() {
        return subscriptionEnd != null && LocalDate.now().isAfter(subscriptionEnd);
    }
}
